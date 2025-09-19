package com.example.quizapp.controllers;

import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.model.TournamentQuestion;
import com.example.quizapp.services.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/start/{tournamentId}")
    public ResponseEntity<?> startAttempt(@PathVariable Long tournamentId) {
        try {
            PlayerAttempt attempt = service.startAttempt(tournamentId);
            return ResponseEntity.ok(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/finish/{attemptId}")
    public ResponseEntity<?> finishAttempt(@PathVariable Long attemptId) {
        try {
            PlayerAttempt attempt = service.finishAttempt(attemptId);
            return ResponseEntity.ok(attempt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/submit-answer")
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> request) {
        try {
            Long attemptId = Long.valueOf(request.get("attemptId").toString());
            Long tqId = Long.valueOf(request.get("tqId").toString());
            String selectedAnswer = request.get("selectedAnswer").toString();

            PlayerAnswer answer = service.submitAnswer(attemptId, tqId, selectedAnswer);
            return ResponseEntity.ok(answer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/tournament/{tournamentId}/questions")
    public ResponseEntity<?> getTournamentQuestions(@PathVariable Long tournamentId) {
        List<TournamentQuestion> questions = service.getTournamentQuestions(tournamentId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/my-attempts")
    public ResponseEntity<?> getMyAttempts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        List<PlayerAttempt> attempts = service.getPlayerAttempts(username);
        return ResponseEntity.ok(attempts);
    }

}
