package com.example.quizapp.services;

import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.QuestionRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.dto.ScoreboardResponse;
import com.example.quizapp.dto.ScoreboardResponse.PlayerScore;
import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.db.PlayerAnswerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizTournamentService {

    private final QuizTournamentRepository repo;
    private final QuestionRepository questionRepo;
    private final TournamentQuestionRepository tqRepo;
    private final PlayerAttemptRepository playerAttemptRepo;
    private final PlayerAnswerRepository answerRepo;

    public QuizTournamentService(
            QuizTournamentRepository repo,
            QuestionRepository questionRepo,
            TournamentQuestionRepository tqRepo,
            PlayerAttemptRepository playerAttemptRepo,
            PlayerAnswerRepository answerRepo
    ) {
        this.repo = repo;
        this.questionRepo = questionRepo;
        this.tqRepo = tqRepo;
        this.playerAttemptRepo = playerAttemptRepo;
        this.answerRepo = answerRepo;
    }

    // 🔹 Create tournament and auto-fetch 10 questions from OpenTDB
    public QuizTournament createTournament(QuizTournament quiz) {
        QuizTournament saved = repo.save(quiz);
        System.out.println("✅ Saved tournament with ID: " + saved.getId());

        try {
            String url = "https://opentdb.com/api.php?amount=10&type=multiple";
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                return saved;
            }

            Map<String, Object> response = responseEntity.getBody();
            if (response == null || !response.containsKey("results")) {
                return saved;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return saved;
            }

            int order = 1;
            for (Map<String, Object> q : results) {
                Question question = new Question();
                question.setQuestionText(decodeHtmlEntities((String) q.get("question")));
                question.setCorrectAnswer(decodeHtmlEntities((String) q.get("correct_answer")));

                List<String> incorrect = (List<String>) q.get("incorrect_answers");
                if (incorrect != null) {
                    Collections.shuffle(incorrect);
                    question.setOptionA(question.getCorrectAnswer());
                    if (incorrect.size() >= 3) {
                        question.setOptionB(decodeHtmlEntities(incorrect.get(0)));
                        question.setOptionC(decodeHtmlEntities(incorrect.get(1)));
                        question.setOptionD(decodeHtmlEntities(incorrect.get(2)));
                    }
                }

                Question savedQ = questionRepo.save(question);

                TournamentQuestion tq = new TournamentQuestion();
                tq.setTournament(saved);
                tq.setQuestion(savedQ);
                tq.setQuestionOrder(order++);
                tqRepo.save(tq);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return saved;
    }

    // Helper method
    private String decodeHtmlEntities(String text) {
        if (text == null) return null;
        return text.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&");
    }

    public List<QuizTournament> getAllTournaments() {
        return repo.findAll();
    }

    public QuizTournament updateTournament(Long id, QuizTournament updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setStartDate(updated.getStartDate());
                    existing.setEndDate(updated.getEndDate());
                    return repo.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));
    }

    public void deleteTournament(Long id) {
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Find all attempts for this tournament
        List<PlayerAttempt> attempts = playerAttemptRepo.findByTournamentId(id);

        // For each attempt, delete answers first
        for (PlayerAttempt attempt : attempts) {
            answerRepo.deleteAll(answerRepo.findByAttempt(attempt));
        }

        // Delete attempts
        playerAttemptRepo.deleteAll(attempts);

        //  Delete tournament questions
        tqRepo.deleteAll(tqRepo.findByTournamentId(id));

        // Finally delete the tournament
        repo.deleteById(id);
    }


    public int addLike(Long id) {
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        tournament.setLikes(tournament.getLikes() + 1);
        repo.save(tournament);
        return tournament.getLikes();
    }

    public int removeLike(Long id) {
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        tournament.setLikes(Math.max(0, tournament.getLikes() - 1));
        repo.save(tournament);
        return tournament.getLikes();
    }

    public ScoreboardResponse getScoreboard(Long tournamentId) {
        QuizTournament tournament = repo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        List<PlayerAttempt> attempts = playerAttemptRepo
                .findByTournamentIdAndCompletedTrueOrderByScoreDesc(tournamentId);

        List<PlayerScore> scores = attempts.stream()
                .map(a -> new PlayerScore(
                        a.getPlayer().getUsername(),
                        a.getScore(),
                        a.getCompletedAt()
                ))
                .toList();

        long totalPlayers = playerAttemptRepo.countDistinctPlayersByTournamentId(tournamentId);
        double avgScore = Optional.ofNullable(
                playerAttemptRepo.findAverageScoreByTournamentId(tournamentId)
        ).orElse(0.0);

        ScoreboardResponse response = new ScoreboardResponse();
        response.setTournamentName(tournament.getName());
        response.setLikes(tournament.getLikes());
        response.setTotalPlayers(totalPlayers);
        response.setAverageScore(avgScore);
        response.setScores(scores);

        return response;
    }
}
