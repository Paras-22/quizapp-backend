package com.example.quizapp.db;

import com.example.quizapp.model.QuizTournament;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizTournamentRepository extends JpaRepository<QuizTournament, Long> {
}
