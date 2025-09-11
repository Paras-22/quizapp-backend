package com.example.quizapp.controllers;

import com.example.quizapp.dto.ScoreboardResponse;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.services.QuizTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
public class QuizTournamentController {

    private final QuizTournamentService service;

    public QuizTournamentController(QuizTournamentService service) {
        this.service = service;
    }

    private String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }

    // ADMIN ONLY - Create tournament
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody QuizTournament quiz) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        QuizTournament created = service.createTournament(quiz);
        return ResponseEntity.ok(created);
    }

    // BOTH - Get all tournaments
    @GetMapping
    public ResponseEntity<List<QuizTournament>> getAll() {
        return ResponseEntity.ok(service.getAllTournaments());
    }

    // ADMIN ONLY - Update tournament
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody QuizTournament updated) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
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
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
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
        if (!"PLAYER".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Players only");
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
        if (!"PLAYER".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Players only");
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