package com.example.quizapp.db;

import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Here I add repository interface for User entity with custom query methods
public interface UserRepository extends JpaRepository<User, Long> {

    // Here I add method to find user by username
    Optional<User> findByUsername(String username);

    // Here I add method to find user by email
    Optional<User> findByEmail(String email);

    // Here I add method to find users by role (e.g., PLAYER, ADMIN)
    List<User> findByRole(Role role);
}
