package com.example.quizapp.controllers;

import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import com.example.quizapp.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        return ResponseEntity.ok(service.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        Optional<User> user = service.login(username, password);
        if (user.isPresent()) {
            return ResponseEntity.ok("Login successful. Role: " + user.get().getRole());
        }
        return ResponseEntity.badRequest().body("Invalid username or password");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable Long id, @Valid @RequestBody User updated) {
        return ResponseEntity.ok(service.updateProfile(id, updated));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return ResponseEntity.ok(service.resetPassword(email));
    }

    // 🔹 Role check endpoints (for testing)
    @GetMapping("/is-admin/{username}")
    public ResponseEntity<Boolean> checkAdmin(@PathVariable String username) {
        return ResponseEntity.ok(service.isAdmin(username));
    }

    @GetMapping("/is-player/{username}")
    public ResponseEntity<Boolean> checkPlayer(@PathVariable String username) {
        return ResponseEntity.ok(service.isPlayer(username));
    }
}
