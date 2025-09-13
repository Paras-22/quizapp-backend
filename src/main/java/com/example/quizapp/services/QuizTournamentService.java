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
import com.example.quizapp.db.PlayerAnswerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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
    private final EmailService emailService;

    public QuizTournamentService(
            QuizTournamentRepository repo,
            QuestionRepository questionRepo,
            TournamentQuestionRepository tqRepo,
            PlayerAttemptRepository playerAttemptRepo,
            PlayerAnswerRepository answerRepo,
            EmailService emailService
    ) {
        // Here I add constructor injection for all required repositories and services
        this.repo = repo;
        this.questionRepo = questionRepo;
        this.tqRepo = tqRepo;
        this.playerAttemptRepo = playerAttemptRepo;
        this.answerRepo = answerRepo;
        this.emailService = emailService;
    }

    // Enhanced create tournament with email notification
    public QuizTournament createTournament(QuizTournament quiz) {
        // Here I add tournament saving logic
        QuizTournament saved = repo.save(quiz);
        System.out.println("✅ Saved tournament with ID: " + saved.getId());

        try {
            // Here I add external API call to fetch questions
            String url = "https://opentdb.com/api.php?amount=10&type=multiple";
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);

            // Here I add response parsing and question creation
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> response = responseEntity.getBody();
                if (response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    if (results != null && !results.isEmpty()) {
                        int order = 1;
                        for (Map<String, Object> q : results) {
                            Question question = new Question();
                            question.setQuestionText(decodeHtmlEntities((String) q.get("question")));
                            question.setCorrectAnswer(decodeHtmlEntities((String) q.get("correct_answer")));

                            List<String> incorrect = (List<String>) q.get("incorrect_answers");
                            if (incorrect != null && incorrect.size() >= 3) {
                                // Here I add shuffling and option assignment
                                Collections.shuffle(incorrect);
                                question.setOptionA(question.getCorrectAnswer());
                                question.setOptionB(decodeHtmlEntities(incorrect.get(0)));
                                question.setOptionC(decodeHtmlEntities(incorrect.get(1)));
                                question.setOptionD(decodeHtmlEntities(incorrect.get(2)));

                                // Here I add question saving and tournament-question linking
                                Question savedQ = questionRepo.save(question);

                                TournamentQuestion tq = new TournamentQuestion();
                                tq.setTournament(saved);
                                tq.setQuestion(savedQ);
                                tq.setQuestionOrder(order++);
                                tqRepo.save(tq);
                            }
                        }
                    }
                }
            }

            // Here I add email notification to all players
            emailService.sendTournamentCreatedNotification(saved.getName());

        } catch (Exception e) {
            // Here I add error handling for API or DB failures
            e.printStackTrace();
        }

        return saved;
    }

    // Get tournaments by status for players
    public Map<String, List<QuizTournament>> getTournamentsByStatus() {
        // Here I add logic to categorize tournaments by date
        List<QuizTournament> allTournaments = repo.findAll();
        LocalDate today = LocalDate.now();

        List<QuizTournament> upcoming = allTournaments.stream()
                .filter(t -> t.getStartDate().isAfter(today))
                .toList();

        List<QuizTournament> ongoing = allTournaments.stream()
                .filter(t -> !t.getStartDate().isAfter(today) && !t.getEndDate().isBefore(today))
                .toList();

        List<QuizTournament> past = allTournaments.stream()
                .filter(t -> t.getEndDate().isBefore(today))
                .toList();

        // Here I add map response with all categories
        return Map.of(
                "upcoming", upcoming,
                "ongoing", ongoing,
                "past", past,
                "all", allTournaments
        );
    }

    // Check if tournament is available for participation
    public boolean canParticipate(Long tournamentId, String username) {
        // Here I add tournament lookup
        QuizTournament tournament = repo.findById(tournamentId).orElse(null);
        if (tournament == null) return false;

        // Here I add date check for ongoing status
        LocalDate today = LocalDate.now();
        boolean isOngoing = !tournament.getStartDate().isAfter(today) &&
                !tournament.getEndDate().isBefore(today);

        if (!isOngoing) return false;

        // Here I add participation check
        return playerAttemptRepo.findByPlayerUsernameAndTournamentId(username, tournamentId)
                .isEmpty();
    }

    private String decodeHtmlEntities(String text) {
        // Here I add basic HTML entity decoding
        if (text == null) return null;
        return text.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    public List<QuizTournament> getAllTournaments() {
        // Here I add fetch-all logic for tournaments
        return repo.findAll();
    }

    public QuizTournament updateTournament(Long id, QuizTournament updated) {
        // Here I add update logic with fallback if tournament not found
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
        // Here I add tournament lookup and deletion
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Clean up related data
        List<PlayerAttempt> attempts = playerAttemptRepo.findByTournamentId(id);
        for (PlayerAttempt attempt : attempts) {
            // Here I add deletion of player answers
            answerRepo.deleteAll(answerRepo.findByAttempt(attempt));
        }
        // Here I add deletion of player attempts and tournament questions
        playerAttemptRepo.deleteAll(attempts);
        tqRepo.deleteAll(tqRepo.findByTournamentId(id));

        repo.deleteById(id);
    }

    public int addLike(Long id) {
        // Here I add logic to increment tournament likes
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        tournament.setLikes(tournament.getLikes() + 1);
        repo.save(tournament);
        return tournament.getLikes();
    }

    public int removeLike(Long id) {
        // Here I add logic to decrement tournament likes safely
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        tournament.setLikes(Math.max(0, tournament.getLikes() - 1));
        repo.save(tournament);
        return tournament.getLikes();
    }

    public ScoreboardResponse getScoreboard(Long tournamentId) {
        // Here I add tournament lookup
        QuizTournament tournament = repo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Here I add fetching of completed attempts sorted by score
        List<PlayerAttempt> attempts = playerAttemptRepo
                .findByTournamentIdAndCompletedTrueOrderByScoreDesc(tournamentId);

        // Here I add mapping of attempts to scoreboard entries
        List<PlayerScore> scores = attempts.stream()
                .map(a -> new PlayerScore(
                        a.getPlayer().getUsername(),
                        a.getScore(),
                        a.getCompletedAt()
                ))
                .toList();

        // Here I add total player count and average score calculation
        long totalPlayers = playerAttemptRepo.countDistinctPlayersByTournamentId(tournamentId);
        double avgScore = Optional.ofNullable(
                playerAttemptRepo.findAverageScoreByTournamentId(tournamentId)
        ).orElse(0.0);

        // Here I add scoreboard response construction
        ScoreboardResponse response = new ScoreboardResponse();
        response.setTournamentName(tournament.getName());
        response.setLikes(tournament.getLikes());
        response.setTotalPlayers(totalPlayers);
        response.setAverageScore(avgScore);
        response.setScores(scores);

        return response;
    }
}
