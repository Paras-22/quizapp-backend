package com.example.quizapp.db;

import com.example.quizapp.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepo extends JpaRepository<Question, Long> {
}
