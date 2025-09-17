package com.example.quizapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ScoreboardResponse {
    private String tournamentName; // name of the tournament
    private int likes; // total likes
    private long totalPlayers; // number of players participated
    private double averageScore; // average score of completed attempts
    private List<PlayerScore> scores; // list of individual player scores

    public static class PlayerScore {
        private String playerName;
        private int score;
        private LocalDateTime completedAt;

        public PlayerScore(String playerName, int score, LocalDateTime completedAt) {
            this.playerName = playerName;
            this.score = score;
            this.completedAt = completedAt;
        }

        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public long getTotalPlayers() { return totalPlayers; }
    public void setTotalPlayers(long totalPlayers) { this.totalPlayers = totalPlayers; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public List<PlayerScore> getScores() { return scores; }
    public void setScores(List<PlayerScore> scores) { this.scores = scores; }
}
