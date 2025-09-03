package com.example.quizapp.services;

import com.example.quizapp.db.PlayerAnswerRepository;
import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.db.UserRepository;
import com.example.quizapp.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerAttemptRepository attemptRepo;
    private final PlayerAnswerRepository answerRepo;
    private final TournamentQuestionRepository tqRepo;
    private final QuizTournamentRepository tournamentRepo;
    private final UserRepository userRepo;

    public PlayerService(
            PlayerAttemptRepository attemptRepo,
            PlayerAnswerRepository answerRepo,
            TournamentQuestionRepository tqRepo,
            QuizTournamentRepository tournamentRepo,
            UserRepository userRepo) {
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.tqRepo = tqRepo;
        this.tournamentRepo = tournamentRepo;
        this.userRepo = userRepo;
    }

    // Start a new attempt
    public PlayerAttempt startAttempt(String username, Long tournamentId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizTournament tournament = tournamentRepo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        return attemptRepo.findByUserUsernameAndTournamentId(username, tournamentId)
                .orElseGet(() -> {
                    PlayerAttempt attempt = new PlayerAttempt();
                    attempt.setUser(user);
                    attempt.setTournament(tournament);
                    attempt.setStartedAt(LocalDateTime.now());
                    return attemptRepo.save(attempt);
                });
    }

    // Fetch all questions of a tournament
    public List<TournamentQuestion> getTournamentQuestions(Long tournamentId) {
        return tqRepo.findByTournamentIdOrderById(tournamentId);
    }

    // Submit answer
    public PlayerAnswer submitAnswer(Long attemptId, Long questionId, String selectedAnswer) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        TournamentQuestion question = tqRepo.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        PlayerAnswer answer = new PlayerAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedAnswer(selectedAnswer);

        boolean correct = question.getCorrectAnswer().equalsIgnoreCase(selectedAnswer);
        answer.setCorrect(correct);

        if (correct) {
            attempt.setScore(attempt.getScore() + 1);
        }

        answerRepo.save(answer);
        attemptRepo.save(attempt);

        return answer;
    }

    // Finish attempt
    public PlayerAttempt finishAttempt(Long attemptId) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setCompleted(true);
        attempt.setFinishedAt(LocalDateTime.now());
        return attemptRepo.save(attempt);
    }
}
