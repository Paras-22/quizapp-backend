package com.example.quizapp.db;

import com.example.quizapp.model.PlayerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerAttemptRepository extends JpaRepository<PlayerAttempt, Long> {
    // FIXED: Changed from findByUserUsername to findByPlayerUsername
    Optional<PlayerAttempt> findByPlayerUsernameAndTournamentId(String username, Long tournamentId);

    // FIXED: Changed from findByUserUsername to findByPlayerUsername
    List<PlayerAttempt> findByPlayerUsername(String username);
}