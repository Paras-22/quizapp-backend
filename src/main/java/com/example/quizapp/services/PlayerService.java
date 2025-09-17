package com.example.quizapp.services;

import com.example.quizapp.db.PlayerAnswerRepository;
import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.UserRepository;
import com.example.quizapp.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // Here I add constructor injection for all required repositories
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.tqRepo = tqRepo;
        this.tournamentRepo = tournamentRepo;
        this.userRepo = userRepo;
    }

    private String getCurrentUsername() {
        // Here I add logic to fetch current authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public PlayerAttempt startAttempt(Long tournamentId) {
        // Here I add logic to start a new attempt for the current player
        String username = getCurrentUsername();

        User player = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizTournament tournament = tournamentRepo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Here I add check to prevent duplicate attempts
        if (attemptRepo.findByPlayerUsernameAndTournamentId(username, tournamentId).isPresent()) {
            throw new RuntimeException("Player has already started this tournament");
        }

        // Here I add attempt initialization
        PlayerAttempt attempt = new PlayerAttempt();
        attempt.setPlayer(player);
        attempt.setTournament(tournament);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setScore(0);
        attempt.setCompleted(false);

        return attemptRepo.save(attempt);
    }

    public List<TournamentQuestion> getTournamentQuestions(Long tournamentId) {
        // Here I add logic to fetch all questions for a tournament
        return tqRepo.findByTournamentId(tournamentId);
    }

    public PlayerAnswer submitAnswer(Long attemptId, Long tqId, String selectedAnswer) {
        // Here I add logic to submit an answer for a question
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

        // Here I add logic to map selected option to actual answer
        String actualAnswer = null;
        switch (selectedAnswer.toUpperCase()) {
            case "A" -> actualAnswer = question.getOptionA();
            case "B" -> actualAnswer = question.getOptionB();
            case "C" -> actualAnswer = question.getOptionC();
            case "D" -> actualAnswer = question.getOptionD();
            default -> throw new RuntimeException("Invalid option: " + selectedAnswer);
        }

        // Here I add correctness check
        boolean correct = actualAnswer != null &&
                actualAnswer.equalsIgnoreCase(question.getCorrectAnswer());

        // Here I add answer object creation
        PlayerAnswer answer = new PlayerAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedAnswer(selectedAnswer);
        answer.setCorrect(correct);
        answer.setAnsweredAt(LocalDateTime.now());

        // Here I add score update if answer is correct
        if (correct) {
            attempt.setScore(attempt.getScore() + 1);
            attemptRepo.save(attempt);
        }

        return answerRepo.save(answer);
    }

    public PlayerAttempt finishAttempt(Long attemptId) {
        // Here I add logic to mark an attempt as completed
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
        // Here I add logic to fetch all attempts by a player
        return attemptRepo.findByPlayerUsername(username);
    }

    public PlayerAttempt getAttemptById(Long attemptId) {
        // Here I add logic to fetch a specific attempt by ID
        return attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
    }

    // Additional method for detailed attempt history
    public Map<String, Object> getDetailedAttemptHistory(String username) {
        // Here I add logic to compile detailed stats for player's attempts
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

    // Additional method for global leaderboard position
    public Map<String, Object> getGlobalLeaderboardPosition(String username) {
        // Here I add simplified logic to return leaderboard stats
        List<PlayerAttempt> allCompletedAttempts = attemptRepo.findByCompletedTrueOrderByScoreDesc();

        Map<String, Object> position = new HashMap<>();
        position.put("globalRank", "Not implemented yet");
        position.put("totalPlayers", allCompletedAttempts.size());
        position.put("playerAttempts", attemptRepo.findByPlayerUsername(username).size());

        return position;
    }
}
