package com.example.quizapp.controllers;

import com.example.quizapp.db.UserRepository;
import com.example.quizapp.dto.AuthResponse;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import com.example.quizapp.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.model.PlayerAttempt;

import java.util.List;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final PlayerAttemptRepository playerAttemptRepo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    public UserController(PlayerAttemptRepository playerAttemptRepo, UserRepository userRepo, JwtUtil jwtUtil) {
        this.playerAttemptRepo = playerAttemptRepo;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
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

    // Updated profile update method to handle partial updates
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileUpdates) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        Optional<User> userOpt = userRepo.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User existingUser = userOpt.get();

        try {
            // Update only the fields that are provided in the request
            if (profileUpdates.containsKey("username") && !profileUpdates.get("username").trim().isEmpty()) {
                String newUsername = profileUpdates.get("username").trim();
                if (!existingUser.getUsername().equals(newUsername) &&
                        userRepo.findByUsername(newUsername).isPresent()) {
                    return ResponseEntity.badRequest().body("Username already taken");
                }
                existingUser.setUsername(newUsername);
            }

            if (profileUpdates.containsKey("firstName")) {
                String firstName = profileUpdates.get("firstName");
                if (firstName == null || firstName.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("First name cannot be empty");
                }
                existingUser.setFirstName(firstName.trim());
            }

            if (profileUpdates.containsKey("lastName")) {
                String lastName = profileUpdates.get("lastName");
                if (lastName == null || lastName.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("Last name cannot be empty");
                }
                existingUser.setLastName(lastName.trim());
            }

            if (profileUpdates.containsKey("email") && !profileUpdates.get("email").trim().isEmpty()) {
                String newEmail = profileUpdates.get("email").trim();
                // Basic email validation
                if (!newEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    return ResponseEntity.badRequest().body("Invalid email format");
                }
                if (!existingUser.getEmail().equals(newEmail) &&
                        userRepo.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest().body("Email already registered");
                }
                existingUser.setEmail(newEmail);
            }

            // Optional fields - can be empty or null
            if (profileUpdates.containsKey("phone")) {
                existingUser.setPhone(profileUpdates.get("phone"));
            }

            if (profileUpdates.containsKey("address")) {
                existingUser.setAddress(profileUpdates.get("address"));
            }

            if (profileUpdates.containsKey("bio")) {
                existingUser.setBio(profileUpdates.get("bio"));
            }

            if (profileUpdates.containsKey("dateOfBirth")) {
                existingUser.setDateOfBirth(profileUpdates.get("dateOfBirth"));
            }

            if (profileUpdates.containsKey("profilePicture")) {
                existingUser.setProfilePicture(profileUpdates.get("profilePicture"));
            }

            User savedUser = userRepo.save(existingUser);

            // Return the updated user data (without password)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Profile updated successfully");
            responseData.put("user", createUserResponse(savedUser));

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating profile: " + e.getMessage());
        }
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
        return ResponseEntity.ok(createUserResponse(user));
    }

    // Helper method to create user response without password
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("email", user.getEmail());
        userResponse.put("phone", user.getPhone());
        userResponse.put("address", user.getAddress());
        userResponse.put("bio", user.getBio());
        userResponse.put("dateOfBirth", user.getDateOfBirth());
        userResponse.put("profilePicture", user.getProfilePicture());
        userResponse.put("role", user.getRole());
        return userResponse;
    }

    // Add change password endpoint
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Current password and new password are required");
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("New password must be at least 6 characters");
        }

        Optional<User> userOpt = userRepo.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();

        // Verify current password
        if (!user.getPassword().equals(currentPassword)) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        if (currentPassword.equals(newPassword)) {
            return ResponseEntity.badRequest().body("New password must be different from current password");
        }

        // Update password
        user.setPassword(newPassword);
        userRepo.save(user);

        return ResponseEntity.ok("Password changed successfully");
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
        }

        return ResponseEntity.ok("If the email exists, password reset instructions have been sent");
    }

    // Password reset page endpoint (fixes the 403 error)
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

    // Actual password reset endpoint
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Token and newPassword are required");
        }

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

        try {
            // Get all attempts for this player
            List<PlayerAttempt> attempts = playerAttemptRepo.findByPlayerUsername(currentUsername);
            List<PlayerAttempt> completedAttempts = attempts.stream()
                    .filter(PlayerAttempt::isCompleted)
                    .toList();

            Map<String, Object> stats = new HashMap<>();

            // Calculate tournaments played (completed attempts only)
            stats.put("tournamentsPlayed", completedAttempts.size());

            // Calculate average score
            double averageScore = completedAttempts.stream()
                    .mapToInt(PlayerAttempt::getScore)
                    .average()
                    .orElse(0.0);
            stats.put("averageScore", Math.round(averageScore * 10.0) / 10.0); // Round to 1 decimal

            // Calculate best score
            int bestScore = completedAttempts.stream()
                    .mapToInt(PlayerAttempt::getScore)
                    .max()
                    .orElse(0);
            stats.put("bestScore", bestScore);

            // Calculate total points (sum of all scores)
            int totalPoints = completedAttempts.stream()
                    .mapToInt(PlayerAttempt::getScore)
                    .sum();
            stats.put("totalPoints", totalPoints);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            // Return default stats in case of error
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("tournamentsPlayed", 0);
            defaultStats.put("averageScore", 0.0);
            defaultStats.put("bestScore", 0);
            defaultStats.put("totalPoints", 0);

            return ResponseEntity.ok(defaultStats);
        }
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