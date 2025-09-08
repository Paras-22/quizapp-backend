package com.example.quizapp.services;

import com.example.quizapp.db.PlayerAnswerRepository;
import com.example.quizapp.db.PlayerAttemptRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.db.QuizTournamentRepository;
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
            UserRepository userRepo
    ) {
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.tqRepo = tqRepo;
        this.tournamentRepo = tournamentRepo;
        this.userRepo = userRepo;
    }

    // 🔹 Start attempt
    public PlayerAttempt startAttempt(String username, Long tournamentId) {
        User player = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizTournament tournament = tournamentRepo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        PlayerAttempt attempt = new PlayerAttempt();
        attempt.setPlayer(player);
        attempt.setTournament(tournament);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setScore(0);
        attempt.setCompleted(false);

        return attemptRepo.save(attempt);
    }

    // 🔹 Get tournament questions
    public List<TournamentQuestion> getTournamentQuestions(Long tournamentId) {
        return tqRepo.findByTournamentId(tournamentId);
    }

    // 🔹 Submit answer - FIXED mapping option letter to value
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

        // ✅ Map option letter (A/B/C/D) to actual value
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
        answer.setSelectedAnswer(selectedAnswer); // keep the letter the player chose
        answer.setCorrect(correct);
        answer.setAnsweredAt(LocalDateTime.now());

        if (correct) {
            attempt.setScore(attempt.getScore() + 1);
            attemptRepo.save(attempt);
        }

        return answerRepo.save(answer);
    }

    // 🔹 Finish attempt
    public PlayerAttempt finishAttempt(Long attemptId) {
        PlayerAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        attempt.setCompleted(true);
        attempt.setEndTime(LocalDateTime.now());

        return attemptRepo.save(attempt);
    }
}
