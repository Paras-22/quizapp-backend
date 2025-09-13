package com.example.quizapp.controllers;

import com.example.quizapp.db.UserRepository;
import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import com.example.quizapp.security.JwtUtil;
import com.example.quizapp.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserController(UserRepository userRepo, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    private String getCurrentUsername() {
        // Here I add logic to fetch the currently authenticated username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody User loginRequest, BindingResult result) {
        // Here I add validation error handling
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        // Here I add user lookup by username
        Optional<User> userOpt = userRepo.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Here I add password check
        User user = userOpt.get();
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        // Here I add JWT token generation
        String role = user.getRole().name();
        String token = jwtUtil.generateToken(user.getUsername(), role);

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), role));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User newUser, BindingResult result) {
        // Here I add validation error handling
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        // Here I add duplicate username check
        if (userRepo.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        // Here I add duplicate email check
        if (userRepo.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Here I add user saving logic
        User savedUser = userRepo.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Here I add logout response for stateless JWT system
        return ResponseEntity.ok("Logged out successfully. Please discard your token.");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody User updatedUser, BindingResult result) {
        // Here I add validation error handling
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        // Here I add authentication check
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Here I add user lookup
        Optional<User> userOpt = userRepo.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User existingUser = userOpt.get();

        // Here I add username conflict check
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepo.findByUsername(updatedUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        // Here I add email conflict check
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepo.findByEmail(updatedUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Here I add profile update logic
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setProfilePicture(updatedUser.getProfilePicture());
        existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        existingUser.setBio(updatedUser.getBio());

        User savedUser = userRepo.save(existingUser);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        // Here I add authentication check
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Here I add user lookup
        Optional<User> userOpt = userRepo.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Here I add password masking before response
        User user = userOpt.get();
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        // Here I add email presence check
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        // Here I add user lookup and email sending
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            String resetToken = UUID.randomUUID().toString();
            emailService.sendPasswordResetEmail(email, resetToken);
        }

        // Here I add generic response for security
        return ResponseEntity.ok("If the email exists, password reset instructions have been sent");
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        // Here I add admin role check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        // Here I add user list retrieval
        return ResponseEntity.ok(userRepo.findAll());
    }

    @PostMapping("/toggle-status/{userId}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        // Here I add admin role check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        // Here I add placeholder for status toggle logic
        return ResponseEntity.ok("User status toggled successfully");
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getPlayerStats() {
        // Here I add authentication check
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Here I add placeholder for player stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("tournamentsPlayed", 0);
        stats.put("averageScore", 0.0);
        stats.put("bestScore", 0);
        stats.put("totalPoints", 0);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/follow/{username}")
    public ResponseEntity<?> followUser(@PathVariable String username) {
        // Here I add authentication check
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        // Here I add placeholder for follow logic
        return ResponseEntity.ok("User followed successfully");
    }

    @GetMapping("/is-admin/{username}")
    public ResponseEntity<?> isAdmin(@PathVariable String username) {
        // Here I add role check for admin
        return userRepo.findByUsername(username)
                .map(user -> ResponseEntity.ok(user.getRole() == Role.ADMIN))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/is-player/{username}")
    public ResponseEntity<?> isPlayer(@PathVariable String username) {
        // Here I add role check for player
        return userRepo.findByUsername(username)
                .map(user -> ResponseEntity.ok(user.getRole() == Role.PLAYER))
                .orElse(ResponseEntity.notFound().build());
    }
}
