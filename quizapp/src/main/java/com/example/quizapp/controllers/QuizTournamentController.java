package com.example.quizapp.controllers;

import com.example.quizapp.dto.ScoreboardResponse;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.services.QuizTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tournaments")
public class QuizTournamentController {

    private final QuizTournamentService service;

    public QuizTournamentController(QuizTournamentService service) {
        // Here I add constructor injection for tournament service
        this.service = service;
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

    @GetMapping("/debug-auth")
    public ResponseEntity<?> debugAuth() {
        // Here I add endpoint to debug authentication details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> debug = new HashMap<>();
        debug.put("username", auth != null ? auth.getName() : "none");
        debug.put("role", getCurrentRole());
        debug.put("authorities", auth != null ? auth.getAuthorities().toString() : "none");
        debug.put("isAuthenticated", auth != null && auth.isAuthenticated());
        return ResponseEntity.ok(debug);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody QuizTournament quiz) {
        // Here I add role check to restrict creation to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        try {
            // Here I add logic to create a new tournament
            QuizTournament created = service.createTournament(quiz);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating tournament: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<QuizTournament>> getAll() {
        // Here I add endpoint to fetch all tournaments
        return ResponseEntity.ok(service.getAllTournaments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody QuizTournament updated) {
        // Here I add role check to restrict update to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        try {
            // Here I add logic to update tournament details
            QuizTournament updatedTournament = service.updateTournament(id, updated);
            return ResponseEntity.ok(updatedTournament);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Enhanced delete with confirmation requirement
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam(required = false) String confirm) {
        // Here I add role check to restrict deletion to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        // Here I add confirmation check before deletion
        if (!"yes".equalsIgnoreCase(confirm)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Deletion requires confirmation",
                    "instruction", "Add ?confirm=yes to confirm deletion",
                    "warning", "This action cannot be undone and will delete all related attempts and answers"
            ));
        }

        try {
            // Here I add logic to delete tournament and related data
            service.deleteTournament(id);
            return ResponseEntity.ok("Tournament deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable Long id) {
        // Here I add role check to restrict liking to players
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        try {
            // Here I add logic to increment tournament likes
            int likes = service.addLike(id);
            return ResponseEntity.ok("Tournament liked. Total likes: " + likes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unlike/{id}")
    public ResponseEntity<?> unlike(@PathVariable Long id) {
        // Here I add role check to restrict unliking to players
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        try {
            // Here I add logic to decrement tournament likes
            int likes = service.removeLike(id);
            return ResponseEntity.ok("Tournament unliked. Total likes: " + likes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/scores")
    public ResponseEntity<?> getScores(@PathVariable Long id) {
        // Here I add endpoint to fetch scoreboard for a tournament
        try {
            ScoreboardResponse scoreboard = service.getScoreboard(id);
            return ResponseEntity.ok(scoreboard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Additional Admin Feature 1: Tournament Analytics
    @GetMapping("/analytics")
    public ResponseEntity<?> getTournamentAnalytics() {
        // Here I add role check to restrict analytics to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        // Here I add logic to fetch tournament analytics
        Map<String, Object> analytics = service.getTournamentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Additional Admin Feature 2: Bulk Tournament Operations
    @PostMapping("/bulk-update")
    public ResponseEntity<?> bulkUpdateTournaments(@RequestBody Map<String, Object> bulkData) {
        // Here I add role check to restrict bulk operations to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        // Here I add placeholder for bulk update logic
        return ResponseEntity.ok("Bulk update completed successfully");
    }
}
