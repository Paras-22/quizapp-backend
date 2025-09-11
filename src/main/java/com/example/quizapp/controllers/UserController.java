package com.example.quizapp.controllers;

import com.example.quizapp.db.UserRepository;
import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.model.User;
import com.example.quizapp.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
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
}