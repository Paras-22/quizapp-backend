package com.example.quizapp.controllers;

import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.services.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    // 🔹 Start attempt
    @PostMapping("/start/{username}/{tournamentId}")
    public ResponseEntity<PlayerAttempt> startAttempt(
            @PathVariable String username,
            @PathVariable Long tournamentId
    ) {
        return ResponseEntity.ok(service.startAttempt(username, tournamentId));
    }

    // 🔹 Get tournament questions
    @GetMapping("/questions/{tournamentId}")
    public ResponseEntity<List<TournamentQuestion>> getQuestions(
            @PathVariable Long tournamentId
    ) {
        return ResponseEntity.ok(service.getTournamentQuestions(tournamentId));
    }

    // 🔹 Submit answer
    @PostMapping("/answer/{attemptId}/{tqId}")
    public ResponseEntity<PlayerAnswer> submitAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long tqId,
            @RequestParam String selectedAnswer
    ) {
        return ResponseEntity.ok(service.submitAnswer(attemptId, tqId, selectedAnswer));
    }

    // 🔹 Finish attempt
    @PostMapping("/finish/{attemptId}")
    public ResponseEntity<PlayerAttempt> finishAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(service.finishAttempt(attemptId));
    }
}
