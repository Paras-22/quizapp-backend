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
        this.service = service;
    }

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "NONE";
        }
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    // DEBUG ENDPOINT - Test this first
    @GetMapping("/debug-auth")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> debug = new HashMap<>();
        debug.put("username", getCurrentUsername());
        debug.put("role", getCurrentRole());
        debug.put("authorities", auth != null ? auth.getAuthorities().toString() : "none");
        debug.put("isAuthenticated", auth != null && auth.isAuthenticated());
        return ResponseEntity.ok(debug);
    }

    // ADMIN ONLY - Create tournament
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody QuizTournament quiz) {
        String role = getCurrentRole();
        System.out.println("DEBUG - Create tournament - Current role: " + role);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        try {
            QuizTournament created = service.createTournament(quiz);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating tournament: " + e.getMessage());
        }
    }

    // BOTH - Get all tournaments
    @GetMapping
    public ResponseEntity<List<QuizTournament>> getAll() {
        return ResponseEntity.ok(service.getAllTournaments());
    }

    // ADMIN ONLY - Update tournament
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody QuizTournament updated) {
        String role = getCurrentRole();
        System.out.println("DEBUG - Update tournament - Current role: " + role);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        try {
            QuizTournament updatedTournament = service.updateTournament(id, updated);
            return ResponseEntity.ok(updatedTournament);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ADMIN ONLY - Delete tournament
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        try {
            service.deleteTournament(id);
            return ResponseEntity.ok("Tournament deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PLAYER ONLY - Like tournament
    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable Long id) {
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only. Current role: " + role);
        }
        try {
            int likes = service.addLike(id);
            return ResponseEntity.ok("Tournament liked. Total likes: " + likes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PLAYER ONLY - Unlike tournament
    @PostMapping("/unlike/{id}")
    public ResponseEntity<?> unlike(@PathVariable Long id) {
        String role = getCurrentRole();
        if (!"PLAYER".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Players only. Current role: " + role);
        }
        try {
            int likes = service.removeLike(id);
            return ResponseEntity.ok("Tournament unliked. Total likes: " + likes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // BOTH - View scores/scoreboard
    @GetMapping("/{id}/scores")
    public ResponseEntity<?> getScores(@PathVariable Long id) {
        try {
            ScoreboardResponse scoreboard = service.getScoreboard(id);
            return ResponseEntity.ok(scoreboard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
