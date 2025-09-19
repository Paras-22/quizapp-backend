package com.example.quizapp.controllers;

import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.UserRepository;
import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.model.PlayerAttempt;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import com.example.quizapp.security.JwtUtil;
//import com.example.quizapp.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    //private final EmailService emailService;
    private final PlayerAttemptRepository attemptRepo;

    public UserController(UserRepository userRepo,
                          JwtUtil jwtUtil,
                          //EmailService emailService,
                          PlayerAttemptRepository attemptRepo) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
       // this.emailService = emailService;
        this.attemptRepo = attemptRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepo.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String role = user.getRole().name();
        System.out.println("DEBUG - User role from DB: " + role);

        String token = jwtUtil.generateToken(user.getUsername(), role);
        System.out.println("DEBUG - Generated token for user: " + user.getUsername() + " with role: " + role);

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), role));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        if (userRepo.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        System.out.println("DEBUG - Registering user with role: " + newUser.getRole());
        User savedUser = userRepo.save(newUser);
        System.out.println("DEBUG - Saved user with role: " + savedUser.getRole());

        return ResponseEntity.ok("User registered successfully");

    }
    @GetMapping("/is-admin/{username}")
    public ResponseEntity<?> isAdmin(@PathVariable String username) {
        return userRepo.findByUsername(username)
                .map(user -> ResponseEntity.ok(user.getRole() == Role.ADMIN))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/is-player/{username}")
    public ResponseEntity<?> isPlayer(@PathVariable String username) {
        return userRepo.findByUsername(username)
                .map(user -> ResponseEntity.ok(user.getRole() == Role.PLAYER))
                .orElse(ResponseEntity.notFound().build());
    }
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getPlayerStats() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Fetch all attempts for the user (repository must provide findByPlayerUsername)
        List<PlayerAttempt> attempts = attemptRepo.findByPlayerUsername(currentUsername);

        // Filter only completed attempts (we don't count in-progress)
        List<PlayerAttempt> completed = attempts.stream()
                .filter(PlayerAttempt::isCompleted)
                .collect(Collectors.toList());

        int tournamentsPlayed = completed.size();
        int totalPoints = completed.stream().mapToInt(PlayerAttempt::getScore).sum();
        double averageScore = completed.stream().mapToInt(PlayerAttempt::getScore).average().orElse(0.0);
        int bestScore = completed.stream().mapToInt(PlayerAttempt::getScore).max().orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("tournamentsPlayed", tournamentsPlayed);
        stats.put("bestScore", bestScore);
        stats.put("totalPoints", totalPoints);
        stats.put("averageScore", averageScore);

        return ResponseEntity.ok(stats);
    }
}