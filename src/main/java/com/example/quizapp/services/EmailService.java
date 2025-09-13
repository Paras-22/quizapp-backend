package com.example.quizapp.services;

import com.example.quizapp.db.UserRepository;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private final UserRepository userRepo;

    public EmailService(UserRepository userRepo) {
        // Here I add constructor injection for accessing user data
        this.userRepo = userRepo;
    }

    public void sendPasswordResetEmail(String email, String resetToken) {
        // Here I add mock email logic for password reset
        // In a real application, this would integrate with an actual email provider
        System.out.println("=== EMAIL SENT ===");
        System.out.println("To: " + email);
        System.out.println("Subject: Password Reset Request");
        System.out.println("Reset Token: " + resetToken);
        System.out.println("Reset Link: http://localhost:8080/reset-password?token=" + resetToken);
        System.out.println("==================");
    }

    public void sendTournamentCreatedNotification(String tournamentName) {
        // Here I add logic to fetch all users with PLAYER role
        List<User> players = userRepo.findByRole(Role.PLAYER);

        // Here I add mock email notification for each player
        for (User player : players) {
            System.out.println("=== EMAIL SENT ===");
            System.out.println("To: " + player.getEmail());
            System.out.println("Subject: New Tournament Created: " + tournamentName);
            System.out.println("Body: A new tournament '" + tournamentName + "' has been created. Join now!");
            System.out.println("==================");
        }
    }
}
