package com.example.quizapp.controllers;

import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.services.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    // Start a new attempt
    @PostMapping("/start")
    public ResponseEntity<PlayerAttempt> startAttempt(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        Long tournamentId = Long.parseLong(body.get("tournamentId"));
        return ResponseEntity.ok(service.startAttempt(username, tournamentId));
    }

    // Get tournament questions
    @GetMapping("/questions/{tournamentId}")
    public ResponseEntity<List<TournamentQuestion>> getQuestions(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(service.getTournamentQuestions(tournamentId));
    }

    // Submit answer
    @PostMapping("/answer")
    public ResponseEntity<PlayerAnswer> submitAnswer(@RequestBody Map<String, String> body) {
        Long attemptId = Long.parseLong(body.get("attemptId"));
        Long questionId = Long.parseLong(body.get("questionId"));
        String selectedAnswer = body.get("selectedAnswer");
        return ResponseEntity.ok(service.submitAnswer(attemptId, questionId, selectedAnswer));
    }

    // Finish attempt
    @PostMapping("/finish/{attemptId}")
    public ResponseEntity<PlayerAttempt> finishAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(service.finishAttempt(attemptId));
    }
}
