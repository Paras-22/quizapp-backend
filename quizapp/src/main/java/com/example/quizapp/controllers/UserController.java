package com.example.quizapp.controllers;

import com.example.quizapp.db.UserRepository;
import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import com.example.quizapp.security.JwtUtil;
//import com.example.quizapp.services.EmailService;
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
   // private final EmailService emailService;

    public UserController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        //this.emailService = emailService;
    }
    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "NONE";
        }

        String authority = auth.getAuthorities().iterator().next().getAuthority();

        // Remove ROLE_ prefix if present (Spring Security adds this automatically)
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5); // Returns "ADMIN" instead of "ROLE_ADMIN"
        }
        return authority;
    }
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepo.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String role = user.getRole().name();
        String token = jwtUtil.generateToken(user.getUsername(), role);

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), role));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User newUser, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        if (userRepo.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        if (userRepo.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User savedUser = userRepo.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully. Please discard your token.");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody User updatedUser, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        Optional<User> userOpt = userRepo.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User existingUser = userOpt.get();

        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepo.findByUsername(updatedUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepo.findByEmail(updatedUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

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
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        Optional<User> userOpt = userRepo.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            String resetToken = UUID.randomUUID().toString();
            //emailService.sendPasswordResetEmail(email, resetToken);
        }

        return ResponseEntity.ok("If the email exists, password reset instructions have been sent");
    }

    // NEW: Password reset page endpoint (fixes the 403 error)
    @GetMapping("/reset-password")
    public ResponseEntity<?> showPasswordResetPage(@RequestParam String token) {
        String html = """
            <html>
            <head>
                <title>Password Reset</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; }
                    .container { max-width: 600px; margin: 0 auto; }
                    .token { background: #f0f0f0; padding: 10px; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Password Reset - Quiz App</h2>
                    <p><strong>Reset Token:</strong></p>
                    <div class="token">%s</div>
                    
                    <h3>Mock Implementation Notice</h3>
                    <p>This is a demonstration of the password reset functionality.</p>
                    <p>In a production application, this page would:</p>
                    <ul>
                        <li>Validate the reset token</li>
                        <li>Show a form to enter new password</li>
                        <li>Update the password in database</li>
                        <li>Redirect to login page</li>
                    </ul>
                    
                    <h3>API Testing</h3>
                    <p>You can test the password reset API endpoint:</p>
                    <pre>POST /users/reset-password
{
  "token": "%s",
  "newPassword": "newPassword123"
}</pre>
                    
                    <p><a href="http://localhost:8080/tournaments/debug-auth">← Back to API Testing</a></p>
                </div>
            </body>
            </html>
            """.formatted(token, token);

        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
    }

    // NEW: Actual password reset endpoint
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Token and newPassword are required");
        }

        // Mock implementation - in real app you would:
        // 1. Validate token exists and hasn't expired
        // 2. Find user associated with token
        // 3. Update their password (hash it first!)
        // 4. Delete the reset token

        return ResponseEntity.ok(Map.of(
                "message", "Password reset completed successfully",
                "note", "This is a mock implementation for demonstration"
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        return ResponseEntity.ok(userRepo.findAll());
    }

    @PostMapping("/toggle-status/{userId}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        return ResponseEntity.ok("User status toggled successfully");
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getPlayerStats() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("tournamentsPlayed", 0);
        stats.put("averageScore", 0.0);
        stats.put("bestScore", 0);
        stats.put("totalPoints", 0);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/follow/{username}")
    public ResponseEntity<?> followUser(@PathVariable String username) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        return ResponseEntity.ok("User followed successfully");
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
}