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
        this.repo = repo;
    }

    public User register(User user) {
        return repo.save(user);
    }

    public Optional<User> login(String username, String password) {
        return repo.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }

    public User updateProfile(Long id, User updated) {
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
        return repo.findByEmail(email)
                .map(user -> "Password reset link sent to " + user.getEmail())
                .orElse("Email not found");
    }

    // ✅ Role checks now work with enums
    public boolean isAdmin(String username) {
        return repo.findByUsername(username)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public boolean isPlayer(String username) {
        return repo.findByUsername(username)
                .map(u -> u.getRole() == Role.PLAYER)
                .orElse(false);
    }
}
