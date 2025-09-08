package com.example.quizapp.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_attempts")
public class PlayerAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

//    @ManyToOne
//    @JoinColumn(name = "tournament_id", nullable = false)
//    private QuizTournament tournament;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuizTournament tournament;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private int score;
    private boolean completed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Ensure getters and setters exist for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPlayer() { return player; }
    public void setPlayer(User player) { this.player = player; }

    public QuizTournament getTournament() { return tournament; }
    public void setTournament(QuizTournament tournament) { this.tournament = tournament; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}