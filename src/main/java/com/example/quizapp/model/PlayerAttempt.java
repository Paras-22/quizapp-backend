package com.example.quizapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "player_attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tournament_id"})
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlayerAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id")
    private QuizTournament tournament;


    @Column(nullable = false)
    private int currentIndex = 0;   // 0..9 (10 questions)

    @Column(nullable = false)
    private int score = 0;          // 0..10


    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private boolean completed = false;


    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public QuizTournament getTournament() { return tournament; }
    public void setTournament(QuizTournament tournament) { this.tournament = tournament; }
    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int currentIndex) { this.currentIndex = currentIndex; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
