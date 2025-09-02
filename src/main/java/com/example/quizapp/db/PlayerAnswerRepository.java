package com.example.quizapp.db;

import com.example.quizapp.model.PlayerAnswer;
import com.example.quizapp.model.PlayerAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long> {
    List<PlayerAnswer> findByAttempt(PlayerAttempt attempt);
    long countByAttemptAndCorrectTrue(PlayerAttempt attempt);
}
