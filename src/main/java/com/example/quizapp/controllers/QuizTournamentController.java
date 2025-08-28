package com.example.quizapp.controllers;

import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.services.QuizTournamentService;
import jakarta.validation.Valid;
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

    @PostMapping("/create")
    public ResponseEntity<QuizTournament> createTournament(@Valid @RequestBody QuizTournament quiz) {
        return ResponseEntity.ok(service.createTournament(quiz));
    }

    @GetMapping
    public ResponseEntity<List<QuizTournament>> getAllTournaments() {
        return ResponseEntity.ok(service.getAllTournaments());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<QuizTournament> updateTournament(@PathVariable Long id, @RequestBody QuizTournament updated) {
        return ResponseEntity.ok(service.updateTournament(id, updated));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTournament(@PathVariable Long id) {
        service.deleteTournament(id);
        return ResponseEntity.ok("Tournament deleted successfully");
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<String> likeTournament(@PathVariable Long id) {
        int likes = service.addLike(id);
        return ResponseEntity.ok("Tournament liked. Total likes: " + likes);
    }
}
