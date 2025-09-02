package com.example.quizapp.db;

import com.example.quizapp.model.TournamentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentQuestionRepository extends JpaRepository<TournamentQuestion, Long> {
    List<TournamentQuestion> findByTournamentIdOrderById(Long tournamentId);
    long countByTournamentId(Long tournamentId);
}
