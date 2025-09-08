package com.example.quizapp.controllers;

import com.example.quizapp.dto.ScoreboardResponse;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.services.QuizTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tournaments")
public class QuizTournamentController {

    private final QuizTournamentService service;
    private final com.example.quizapp.db.QuizTournamentRepository repo;
    private final com.example.quizapp.db.TournamentQuestionRepository tqRepo;
    private final com.example.quizapp.db.QuestionRepository questionRepo;
    private final com.example.quizapp.db.PlayerAttemptRepository playerAttemptRepo;

    public QuizTournamentController(
            QuizTournamentService service,
            com.example.quizapp.db.QuizTournamentRepository repo,
            com.example.quizapp.db.TournamentQuestionRepository tqRepo,
            com.example.quizapp.db.QuestionRepository questionRepo,
            com.example.quizapp.db.PlayerAttemptRepository playerAttemptRepo
    ) {
        this.service = service;
        this.repo = repo;
        this.tqRepo = tqRepo;
        this.questionRepo = questionRepo;
        this.playerAttemptRepo = playerAttemptRepo;
    }

    // Create tournament (auto-links 10 random questions)
    @PostMapping("/create")
    public ResponseEntity<QuizTournament> create(@RequestBody QuizTournament quiz) {
        return ResponseEntity.ok(service.createTournament(quiz));
    }

    @GetMapping
    public ResponseEntity<List<QuizTournament>> getAll() {
        return ResponseEntity.ok(service.getAllTournaments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizTournament> update(@PathVariable Long id, @RequestBody QuizTournament updated) {
        return ResponseEntity.ok(service.updateTournament(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteTournament(id);
        return ResponseEntity.ok("Tournament deleted");
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<String> like(@PathVariable Long id) {
        int likes = service.addLike(id);
        return ResponseEntity.ok("Tournament liked. Total likes: " + likes);
    }

    @PostMapping("/unlike/{id}")
    public ResponseEntity<String> unlike(@PathVariable Long id) {
        int likes = service.removeLike(id);
        return ResponseEntity.ok("Tournament unliked. Total likes: " + likes);
    }

    @GetMapping("/{id}/scores")
    public ResponseEntity<ScoreboardResponse> getScores(@PathVariable Long id) {
        return ResponseEntity.ok(service.getScoreboard(id));
    }

    // 🔍 Debug endpoint to check tournament questions
    @GetMapping("/debug/{tournamentId}")
    public ResponseEntity<Map<String, Object>> debugTournament(@PathVariable Long tournamentId) {
        Map<String, Object> debugInfo = new HashMap<>();

        // Check if tournament exists
        Optional<QuizTournament> tournament = repo.findById(tournamentId);
        debugInfo.put("tournamentExists", tournament.isPresent());

        if (tournament.isPresent()) {
            debugInfo.put("tournament", tournament.get());
        }

        // Check tournament questions
        List<TournamentQuestion> tqs = tqRepo.findByTournamentId(tournamentId);
        debugInfo.put("tournamentQuestionsCount", tqs.size());
        debugInfo.put("tournamentQuestions", tqs);

        // Check all questions in database
        List<com.example.quizapp.model.Question> allQuestions = questionRepo.findAll();
        debugInfo.put("totalQuestionsInDB", allQuestions.size());

        System.out.println("🔍 DEBUG: Tournament " + tournamentId + " - " +
                tqs.size() + " linked questions, " +
                allQuestions.size() + " total questions in DB");

        return ResponseEntity.ok(debugInfo);
    }

    // 🛠️ Manual fix endpoint for testing
    @PostMapping("/fix-tournament-questions/{tournamentId}")
    public ResponseEntity<String> fixTournamentQuestions(@PathVariable Long tournamentId) {
        Optional<QuizTournament> tournamentOpt = repo.findById(tournamentId);
        if (!tournamentOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Tournament not found");
        }

        QuizTournament tournament = tournamentOpt.get();
        List<com.example.quizapp.model.Question> allQuestions = questionRepo.findAll();
        int order = 1;
        int linkedCount = 0;

        for (com.example.quizapp.model.Question question : allQuestions) {
            TournamentQuestion tq = new TournamentQuestion();
            tq.setTournament(tournament);
            tq.setQuestion(question);
            tq.setQuestionOrder(order++);
            tqRepo.save(tq);
            linkedCount++;
        }

        return ResponseEntity.ok("Manually linked " + linkedCount + " questions to tournament " + tournamentId);
    }


    @GetMapping("/{tournamentId}/scores")
    public ResponseEntity<Map<String, Object>> getTournamentScores(@PathVariable Long tournamentId) {
        Optional<QuizTournament> tournamentOpt = repo.findById(tournamentId);
        if (!tournamentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        QuizTournament tournament = tournamentOpt.get();

        // Get completed attempts for this tournament
        List<com.example.quizapp.model.PlayerAttempt> attempts =
                playerAttemptRepo.findByTournamentIdAndCompletedTrue(tournamentId);

        // Calculate statistics
        Long totalPlayers = playerAttemptRepo.countDistinctPlayersByTournamentId(tournamentId);
        Double averageScore = playerAttemptRepo.findAverageScoreByTournamentId(tournamentId);

        // Prepare score data
        List<Map<String, Object>> scores = attempts.stream()
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore())) // Descending by score
                .map(attempt -> {
                    Map<String, Object> scoreInfo = new HashMap<>();
                    scoreInfo.put("playerId", attempt.getPlayer().getId());
                    scoreInfo.put("playerName", attempt.getPlayer().getUsername());
                    scoreInfo.put("playerFirstName", attempt.getPlayer().getFirstName());
                    scoreInfo.put("playerLastName", attempt.getPlayer().getLastName());
                    scoreInfo.put("score", attempt.getScore());
                    scoreInfo.put("completedDate", attempt.getEndTime());
                    scoreInfo.put("totalQuestions", 10); // Assuming 10 questions per tournament
                    scoreInfo.put("percentage", (attempt.getScore() * 10)); // Score out of 10 -> percentage
                    scoreInfo.put("passed", attempt.getScore() >= tournament.getMinPassingScore());
                    return scoreInfo;
                })
                .collect(Collectors.toList());

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("tournamentId", tournament.getId());
        response.put("tournamentName", tournament.getName());
        response.put("totalPlayers", totalPlayers != null ? totalPlayers : 0);
        response.put("averageScore", averageScore != null ? Math.round(averageScore * 10.0) / 10.0 : 0);
        response.put("minPassingScore", tournament.getMinPassingScore());
        response.put("likes", tournament.getLikes());
        response.put("scores", scores);

        return ResponseEntity.ok(response);
    }
}