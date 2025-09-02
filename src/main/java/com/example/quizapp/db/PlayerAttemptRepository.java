package com.example.quizapp.db;

import com.example.quizapp.model.PlayerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerAttemptRepository extends JpaRepository<PlayerAttempt, Long> {
    Optional<PlayerAttempt> findByUserUsernameAndTournamentId(String username, Long tournamentId);
    List<PlayerAttempt> findByUserUsername(String username);
}
