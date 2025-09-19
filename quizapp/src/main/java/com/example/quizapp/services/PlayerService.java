package com.example.quizapp.services;

import com.example.quizapp.db.PlayerAnswerRepository;
import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.db.UserRepository;
import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PlayerService
 *
 * - Responsible for player actions (starting attempts, submitting answers, finishing attempts)
 * - Implements getGlobalLeaderboardPosition which computes player's global rank based on best scores
 */
@Service
public class PlayerService {

    private final PlayerAttemptRepository attemptRepo;
    private final PlayerAnswerRepository answerRepo;
    private final TournamentQuestionRepository tqRepo;
    private final QuizTournamentRepository tournamentRepo;
    private final UserRepository userRepo;

    public PlayerService(
            PlayerAttemptRepository attemptRepo,
            PlayerAnswerRepository answerRepo,
            TournamentQuestionRepository tqRepo,
            QuizTournamentRepository tournamentRepo,
            UserRepository userRepo
    ) {
        // Constructor injection for repositories
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.tqRepo = tqRepo;
        this.tournamentRepo = tournamentRepo;
        this.userRepo = userRepo;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? authentication.getName() : null;
    }

    public PlayerAttempt startAttempt(Long tournamentId) {
        String username = getCurrentUsername();

        User player = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizTournament tournament = tournamentRepo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Prevent duplicate attempts for same tournament if your business requires that
        if (attemptRepo.findByPlayerUsernameAndTournamentId(username, tournamentId).isPresent()) {
            throw new RuntimeException("Player has already started this tournament");
        }

        PlayerAttempt attempt = new PlayerAttempt();
        attempt.setPlayer(player);
        attempt.setTournament(tournament);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setScore(0);
        attempt.setCompleted(false);

        return attemptRepo.save(attempt);
    }

    public List<TournamentQuestion> getTournamentQuestions(Long tournamentId) {
        return tqRepo.findByTournamentId(tournamentId);
    }

    public PlayerAnswer submitAnswer(Long attemptId, Long tqId, String selectedAnswer) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.isCompleted()) {
            throw new RuntimeException("This quiz attempt has already been completed");
        }

        TournamentQuestion tq = tqRepo.findById(tqId)
                .orElseThrow(() -> new RuntimeException("Tournament question not found"));

        Question question = tq.getQuestion();
        if (question == null) {
            throw new RuntimeException("Question not found for tournament question");
        }

        // Map letter option (A/B/C/D) to actual answer text if your frontend uses letters
        String actualAnswer;
        switch (selectedAnswer.toUpperCase()) {
            case "A" -> actualAnswer = question.getOptionA();
            case "B" -> actualAnswer = question.getOptionB();
            case "C" -> actualAnswer = question.getOptionC();
            case "D" -> actualAnswer = question.getOptionD();
            default -> throw new RuntimeException("Invalid option: " + selectedAnswer);
        }

        boolean correct = actualAnswer != null && actualAnswer.equalsIgnoreCase(question.getCorrectAnswer());

        PlayerAnswer answer = new PlayerAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedAnswer(selectedAnswer);
        answer.setCorrect(correct);
        answer.setAnsweredAt(LocalDateTime.now());

        if (correct) {
            attempt.setScore(attempt.getScore() + 1);
            attemptRepo.save(attempt);
        }

        return answerRepo.save(answer);
    }

    public PlayerAttempt finishAttempt(Long attemptId) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.isCompleted()) {
            throw new RuntimeException("Attempt already completed");
        }

        attempt.setCompleted(true);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now());

        return attemptRepo.save(attempt);
    }

    public List<PlayerAttempt> getPlayerAttempts(String username) {
        return attemptRepo.findByPlayerUsername(username);
    }

    public PlayerAttempt getAttemptById(Long attemptId) {
        return attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
    }

    public Map<String, Object> getDetailedAttemptHistory(String username) {
        List<PlayerAttempt> attempts = attemptRepo.findByPlayerUsername(username);

        Map<String, Object> history = new HashMap<>();
        history.put("totalAttempts", attempts.size());
        history.put("completedAttempts", attempts.stream().filter(PlayerAttempt::isCompleted).count());
        history.put("averageScore", attempts.stream()
                .filter(PlayerAttempt::isCompleted)
                .mapToInt(PlayerAttempt::getScore)
                .average()
                .orElse(0.0));
        history.put("bestScore", attempts.stream()
                .filter(PlayerAttempt::isCompleted)
                .mapToInt(PlayerAttempt::getScore)
                .max()
                .orElse(0));
        history.put("attempts", attempts);

        return history;
    }

    /**
     * getGlobalLeaderboardPosition - computes the global rank for the given username.
     *
     * Algorithm:
     *  - Fetch completed attempts (or fetch all attempts and filter completed)
     *  - Compute best score per player (map username -> bestScore)
     *  - Sort players by bestScore descending
     *  - Determine the rank of the requested username (1-based). If not present -> "Unranked"
     *  - Return map: { globalRank, totalPlayers, playerAttempts }
     */
    public Map<String, Object> getGlobalLeaderboardPosition(String username) {
        Map<String, Object> position = new HashMap<>();

        // Fetch all completed attempts. If your repository doesn't have findByCompletedTrueOrderByScoreDesc,
        // you can use attemptRepo.findAll() and filter.
        List<PlayerAttempt> allCompletedAttempts;
        try {
            allCompletedAttempts = attemptRepo.findByCompletedTrueOrderByScoreDesc();
        } catch (Exception ex) {
            // Fallback: fetch all and filter manually
            allCompletedAttempts = attemptRepo.findAll().stream()
                    .filter(PlayerAttempt::isCompleted)
                    .collect(Collectors.toList());
        }

        if (allCompletedAttempts.isEmpty()) {
            position.put("globalRank", "No data yet");
            position.put("totalPlayers", 0);
            position.put("playerAttempts", 0);
            return position;
        }

        // Compute best score per player
        Map<String, Integer> bestScores = new HashMap<>();
        for (PlayerAttempt pa : allCompletedAttempts) {
            String playerUsername = pa.getPlayer().getUsername();
            int score = pa.getScore();
            bestScores.merge(playerUsername, score, Math::max);
        }

        // Sort players by best score desc
        List<Map.Entry<String, Integer>> sorted = bestScores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());

        // Find rank of requested username
        Integer rank = null;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(username)) {
                rank = i + 1; // ranks are 1-based
                break;
            }
        }

        long playerAttempts = attemptRepo.findByPlayerUsername(username).size();

        position.put("globalRank", rank == null ? "Unranked" : rank);
        position.put("totalPlayers", bestScores.size());
        position.put("playerAttempts", playerAttempts);

        return position;
    }
}
