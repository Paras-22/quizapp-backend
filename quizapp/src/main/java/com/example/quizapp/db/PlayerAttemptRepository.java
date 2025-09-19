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

    List<PlayerAttempt> findByTournamentId(Long tournamentId);


    List<PlayerAttempt> findByTournamentIdAndCompletedTrue(Long tournamentId);

    //New method: sorted attempts for scoreboard
    List<PlayerAttempt> findByTournamentIdAndCompletedTrueOrderByScoreDesc(Long tournamentId);

    @Query("SELECT COUNT(DISTINCT pa.player) FROM PlayerAttempt pa WHERE pa.tournament.id = :tournamentId AND pa.completed = true")
    Long countDistinctPlayersByTournamentId(@Param("tournamentId") Long tournamentId);

    @Query("SELECT AVG(pa.score) FROM PlayerAttempt pa WHERE pa.tournament.id = :tournamentId AND pa.completed = true")
    Double findAverageScoreByTournamentId(@Param("tournamentId") Long tournamentId);

    List<PlayerAttempt> findByCompletedTrueOrderByScoreDesc();
}
