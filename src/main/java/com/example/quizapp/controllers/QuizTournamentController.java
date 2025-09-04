package com.example.quizapp.controllers;

import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.services.QuizTournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
public class QuizTournamentController {

    private final QuizTournamentService service;

    public QuizTournamentController(QuizTournamentService service) {
        this.service = service;
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
}
