package com.example.quizapp.controllers;

import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.services.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    private String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    @PostMapping("/start/{tournamentId}")
    public ResponseEntity<?> startAttempt(@PathVariable Long tournamentId) {
        if (!"PLAYER".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        PlayerAttempt attempt = service.startAttempt(tournamentId);
        return ResponseEntity.ok(attempt);
    }

    @PostMapping("/finish/{attemptId}")
    public ResponseEntity<?> finishAttempt(@PathVariable Long attemptId) {
        if (!"PLAYER".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Players only");
        }
        PlayerAttempt attempt = service.finishAttempt(attemptId);
        return ResponseEntity.ok(attempt);
    }
}
