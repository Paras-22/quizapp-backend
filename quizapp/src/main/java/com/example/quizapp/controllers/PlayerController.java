package com.example.quizapp.controllers;

import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.services.PlayerService;
import com.example.quizapp.services.QuizTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService service;
    private final QuizTournamentService tournamentService;

    public PlayerController(PlayerService service, QuizTournamentService tournamentService) {
        // Here I add constructor injection for player and tournament services
        this.service = service;
        this.tournamentService = tournamentService;
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "NONE";
        }

        String authority = auth.getAuthorities().iterator().next().getAuthority();

        // Remove ROLE_ prefix if present (Spring Security adds this automatically)
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5); // Returns "ADMIN" instead of "ROLE_ADMIN"
        }
        return authority;
    }

    private String getCurrentUsername() {
        // Here I add logic to fetch current user's username from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // Enhanced tournament filtering by status
    @GetMapping("/tournaments")
    public ResponseEntity<?> getFilteredTournaments(@RequestParam(required = false) String status) {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }

        // Here I add logic to filter tournaments based on status
        String username = getCurrentUsername();
        Map<String, List<QuizTournament>> tournamentsByStatus = tournamentService.getTournamentsByStatus();

        if (status != null) {
            switch (status.toLowerCase()) {
                case "upcoming":
                    return ResponseEntity.ok(Map.of("tournaments", tournamentsByStatus.get("upcoming"), "status", "upcoming"));
                case "ongoing":
                    return ResponseEntity.ok(Map.of("tournaments", tournamentsByStatus.get("ongoing"), "status", "ongoing"));
                case "past":
                    return ResponseEntity.ok(Map.of("tournaments", tournamentsByStatus.get("past"), "status", "past"));
                case "participated":
                    // Here I add logic to fetch tournaments the player has participated in
                    List<PlayerAttempt> attempts = service.getPlayerAttempts(username);
                    List<Long> participatedIds = attempts.stream().map(a -> a.getTournament().getId()).toList();
                    List<QuizTournament> participated = tournamentsByStatus.get("all").stream()
                            .filter(t -> participatedIds.contains(t.getId()))
                            .toList();
                    return ResponseEntity.ok(Map.of("tournaments", participated, "status", "participated"));
                default:
                    return ResponseEntity.badRequest().body("Invalid status. Use: upcoming, ongoing, past, participated");
            }
        }

        return ResponseEntity.ok(tournamentsByStatus);
    }

    @PostMapping("/start/{tournamentId}")
    public ResponseEntity<?> startAttempt(@PathVariable Long tournamentId) {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }

        String username = getCurrentUsername();

        // Here I add participation eligibility check
        if (!tournamentService.canParticipate(tournamentId, username)) {
            return ResponseEntity.badRequest().body("Cannot participate in this tournament. It may be upcoming, past, or you've already participated.");
        }

        try {
            // Here I add logic to start a new attempt
            PlayerAttempt attempt = service.startAttempt(tournamentId);
            return ResponseEntity.ok(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/finish/{attemptId}")
    public ResponseEntity<?> finishAttempt(@PathVariable Long attemptId) {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        try {
            // Here I add logic to finish an attempt
            PlayerAttempt attempt = service.finishAttempt(attemptId);
            return ResponseEntity.ok(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Enhanced submit answer with feedback
    @PostMapping("/submit-answer")
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> request) {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        try {
            // Here I add logic to extract answer submission data
            Long attemptId = Long.valueOf(request.get("attemptId").toString());
            Long tqId = Long.valueOf(request.get("tqId").toString());
            String selectedAnswer = request.get("selectedAnswer").toString();

            // Here I add answer submission and feedback generation
            PlayerAnswer answer = service.submitAnswer(attemptId, tqId, selectedAnswer);

            Map<String, Object> response = new HashMap<>();
            response.put("answer", answer);
            response.put("isCorrect", answer.isCorrect());
            response.put("selectedAnswer", answer.getSelectedAnswer());

            if (!answer.isCorrect()) {
                response.put("correctAnswer", answer.getQuestion().getCorrectAnswer());
                response.put("feedback", "Incorrect! The correct answer was: " + answer.getQuestion().getCorrectAnswer());
            } else {
                response.put("feedback", "Correct! Well done!");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get single question (for separate page presentation)
    @GetMapping("/tournament/{tournamentId}/question/{questionOrder}")
    public ResponseEntity<?> getSingleQuestion(@PathVariable Long tournamentId, @PathVariable int questionOrder) {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }

        try {
            // Here I add logic to fetch a specific question by order
            List<TournamentQuestion> questions = service.getTournamentQuestions(tournamentId);
            TournamentQuestion question = questions.stream()
                    .filter(q -> q.getQuestionOrder() == questionOrder)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("question", question);
            response.put("questionNumber", questionOrder);
            response.put("totalQuestions", questions.size());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/tournament/{tournamentId}/questions")
    public ResponseEntity<?> getTournamentQuestions(@PathVariable Long tournamentId) {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        // Here I add logic to fetch all questions for a tournament
        List<TournamentQuestion> questions = service.getTournamentQuestions(tournamentId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/my-attempts")
    public ResponseEntity<?> getMyAttempts() {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        // Here I add logic to fetch current player's attempts
        String username = getCurrentUsername();
        List<PlayerAttempt> attempts = service.getPlayerAttempts(username);
        return ResponseEntity.ok(attempts);
    }

    // Additional Player Feature 1: Get detailed attempt history
    @GetMapping("/attempt-history")
    public ResponseEntity<?> getDetailedAttemptHistory() {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }

        // Here I add logic to fetch detailed history of player's attempts
        String username = getCurrentUsername();
        Map<String, Object> history = service.getDetailedAttemptHistory(username);
        return ResponseEntity.ok(history);
    }

    // Additional Player Feature 2: Get leaderboard position
    @GetMapping("/leaderboard-position")
    public ResponseEntity<?> getLeaderboardPosition() {
        // Here I add role check to restrict access to players only
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }

        // Here I add logic to fetch player's global leaderboard position
        String username = getCurrentUsername();
        Map<String, Object> position = service.getGlobalLeaderboardPosition(username);
        return ResponseEntity.ok(position);
    }
}
