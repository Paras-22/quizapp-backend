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
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody QuizTournament quiz) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        return ResponseEntity.ok(service.createTournament(quiz));
    }

    @GetMapping
    public ResponseEntity<List<QuizTournament>> getAll() {
        return ResponseEntity.ok(service.getAllTournaments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody QuizTournament updated) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        return ResponseEntity.ok(service.updateTournament(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        service.deleteTournament(id);
        return ResponseEntity.ok("Tournament deleted");
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable Long id) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        int likes = service.addLike(id);
        return ResponseEntity.ok("Tournament liked. Total likes: " + likes);
    }

    @PostMapping("/unlike/{id}")
    public ResponseEntity<?> unlike(@PathVariable Long id) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        int likes = service.removeLike(id);
        return ResponseEntity.ok("Tournament unliked. Total likes: " + likes);
    }

    @GetMapping("/{id}/scores")
    public ResponseEntity<ScoreboardResponse> getScores(@PathVariable Long id) {
        return ResponseEntity.ok(service.getScoreboard(id));
    }
}
