package com.example.quizapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_answers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlayerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id")
    private PlayerAttempt attempt;

    // CHANGE THIS: Store the actual Question instead of TournamentQuestion
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;  // ← Changed from TournamentQuestion to Question

    @Column(nullable = false)
    private String selectedAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    // Getters and setters
    public Long getId() { return id; }
    public PlayerAttempt getAttempt() { return attempt; }
    public void setAttempt(PlayerAttempt attempt) { this.attempt = attempt; }
    public Question getQuestion() { return question; }  // ← Return Question
    public void setQuestion(Question question) { this.question = question; }  // ← Accept Question
    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}