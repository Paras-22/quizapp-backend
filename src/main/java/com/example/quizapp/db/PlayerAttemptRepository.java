package com.example.quizapp.db;

import com.example.quizapp.model.PlayerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerAttemptRepository extends JpaRepository<PlayerAttempt, Long> {
    Optional<PlayerAttempt> findByPlayerUsernameAndTournamentId(String username, Long tournamentId);
    List<PlayerAttempt> findByPlayerUsername(String username);

    // ADD THIS METHOD:
    List<PlayerAttempt> findByTournamentIdAndCompletedTrue(Long tournamentId);

    // ADD THIS METHOD FOR STATISTICS:
    @Query("SELECT COUNT(DISTINCT pa.player) FROM PlayerAttempt pa WHERE pa.tournament.id = :tournamentId AND pa.completed = true")
    Long countDistinctPlayersByTournamentId(@Param("tournamentId") Long tournamentId);

    // ADD THIS METHOD FOR AVERAGE SCORE:
    @Query("SELECT AVG(pa.score) FROM PlayerAttempt pa WHERE pa.tournament.id = :tournamentId AND pa.completed = true")
    Double findAverageScoreByTournamentId(@Param("tournamentId") Long tournamentId);
}