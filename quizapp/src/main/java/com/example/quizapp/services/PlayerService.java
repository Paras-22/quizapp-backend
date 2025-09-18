package com.example.quizapp.services;

import com.example.quizapp.db.PlayerAnswerRepository;
import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.UserRepository;
import com.example.quizapp.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            UserRepository userRepo
    ) {
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.tqRepo = tqRepo;
        this.tournamentRepo = tournamentRepo;
        this.userRepo = userRepo;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public PlayerAttempt startAttempt(Long tournamentId) {
        String username = getCurrentUsername();

        User player = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizTournament tournament = tournamentRepo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        if (attemptRepo.findByPlayerUsernameAndTournamentId(username, tournamentId).isPresent()) {
            throw new RuntimeException("Player has already started this tournament");
        }

        PlayerAttempt attempt = new PlayerAttempt();
        attempt.setPlayer(player);
        attempt.setTournament(tournament);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setScore(0);
        attempt.setCompleted(false);

        return attemptRepo.save(attempt);
    }

    public List<TournamentQuestion> getTournamentQuestions(Long tournamentId) {
        return tqRepo.findByTournamentId(tournamentId);
    }

    public PlayerAnswer submitAnswer(Long attemptId, Long tqId, String selectedAnswer) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.isCompleted()) {
            throw new RuntimeException("This quiz attempt has already been completed");
        }

        TournamentQuestion tq = tqRepo.findById(tqId)
                .orElseThrow(() -> new RuntimeException("Tournament question not found"));

        Question question = tq.getQuestion();
        if (question == null) {
            throw new RuntimeException("Question not found for tournament question");
        }

        String actualAnswer = null;
        switch (selectedAnswer.toUpperCase()) {
            case "A" -> actualAnswer = question.getOptionA();
            case "B" -> actualAnswer = question.getOptionB();
            case "C" -> actualAnswer = question.getOptionC();
            case "D" -> actualAnswer = question.getOptionD();
            default -> throw new RuntimeException("Invalid option: " + selectedAnswer);
        }

        boolean correct = actualAnswer != null &&
                actualAnswer.equalsIgnoreCase(question.getCorrectAnswer());

        PlayerAnswer answer = new PlayerAnswer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedAnswer(selectedAnswer);
        answer.setCorrect(correct);
        answer.setAnsweredAt(LocalDateTime.now());

        if (correct) {
            attempt.setScore(attempt.getScore() + 1);
            attemptRepo.save(attempt);
        }

        return answerRepo.save(answer);
    }

    public PlayerAttempt finishAttempt(Long attemptId) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.isCompleted()) {
            throw new RuntimeException("Attempt already completed");
        }

        attempt.setCompleted(true);
        attempt.setEndTime(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now());

        return attemptRepo.save(attempt);
    }

    public List<PlayerAttempt> getPlayerAttempts(String username) {
        return attemptRepo.findByPlayerUsername(username);
    }

    public PlayerAttempt getAttemptById(Long attemptId) {
        return attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
    }
}