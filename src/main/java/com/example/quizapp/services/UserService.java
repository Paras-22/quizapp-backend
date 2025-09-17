package com.example.quizapp.services;

import com.example.quizapp.db.UserRepository;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        // Here I add constructor injection for UserRepository
        this.repo = repo;
    }

    public User register(User user) {
        // Here I add logic to save a new user
        return repo.save(user);
    }

    public Optional<User> login(String username, String password) {
        // Here I add logic to authenticate user by matching password
        return repo.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }

    public User updateProfile(Long id, User updated) {
        // Here I add logic to update user profile fields
        return repo.findById(id).map(user -> {
            user.setUsername(updated.getUsername());
            user.setFirstName(updated.getFirstName());
            user.setLastName(updated.getLastName());
            user.setEmail(updated.getEmail());
            user.setPhone(updated.getPhone());
            user.setAddress(updated.getAddress());
            return repo.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String resetPassword(String email) {
        // Here I add logic to simulate password reset flow
        return repo.findByEmail(email)
                .map(user -> "Password reset link sent to " + user.getEmail())
                .orElse("Email not found");
    }

    public boolean isAdmin(String username) {
        // Here I add role check for ADMIN
        return repo.findByUsername(username)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public boolean isPlayer(String username) {
        // Here I add role check for PLAYER
        return repo.findByUsername(username)
                .map(u -> u.getRole() == Role.PLAYER)
                .orElse(false);
    }
}
