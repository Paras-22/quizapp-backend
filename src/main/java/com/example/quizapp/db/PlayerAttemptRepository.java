package com.example.quizapp.db;

import com.example.quizapp.model.PlayerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Here I add repository interface for PlayerAttempt entity
public interface PlayerAttemptRepository extends JpaRepository<PlayerAttempt, Long> {

    // Here I add method to find a specific attempt by username and tournament ID
    Optional<PlayerAttempt> findByPlayerUsernameAndTournamentId(String username, Long tournamentId);

    // Here I add method to fetch all attempts by a specific player
    List<PlayerAttempt> findByPlayerUsername(String username);

    // Here I add method to fetch all attempts for a specific tournament
    List<PlayerAttempt> findByTournamentId(Long tournamentId);

    // Here I add method to fetch completed attempts for a tournament
    List<PlayerAttempt> findByTournamentIdAndCompletedTrue(Long tournamentId);

    // Here I add method to fetch completed attempts sorted by score for leaderboard
    List<PlayerAttempt> findByTournamentIdAndCompletedTrueOrderByScoreDesc(Long tournamentId);

    // New method for global leaderboard
    // Here I add custom query to fetch all completed attempts sorted by score
    @Query("SELECT pa FROM PlayerAttempt pa WHERE pa.completed = true ORDER BY pa.score DESC")
    List<PlayerAttempt> findByCompletedTrueOrderByScoreDesc();

    // Here I add query to count distinct players who completed a tournament
    @Query("SELECT COUNT(DISTINCT pa.player) FROM PlayerAttempt pa WHERE pa.tournament.id = :tournamentId AND pa.completed = true")
    Long countDistinctPlayersByTournamentId(@Param("tournamentId") Long tournamentId);

    // Here I add query to calculate average score for a tournament
    @Query("SELECT AVG(pa.score) FROM PlayerAttempt pa WHERE pa.tournament.id = :tournamentId AND pa.completed = true")
    Double findAverageScoreByTournamentId(@Param("tournamentId") Long tournamentId);
}
