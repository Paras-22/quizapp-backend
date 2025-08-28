package com.example.quizapp.services;

import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.model.QuizTournament;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizTournamentService {

    private final QuizTournamentRepository repo;

    public QuizTournamentService(QuizTournamentRepository repo) {
        this.repo = repo;
    }

    public QuizTournament createTournament(QuizTournament quiz) {
        return repo.save(quiz);
    }

    public List<QuizTournament> getAllTournaments() {
        return repo.findAll();
    }

    public QuizTournament updateTournament(Long id, QuizTournament updated) {
        return repo.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setStartDate(updated.getStartDate());
            existing.setEndDate(updated.getEndDate());
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("Tournament not found"));
    }

    public void deleteTournament(Long id) {
        repo.deleteById(id);
    }

    public int addLike(Long id) {
        return repo.findById(id).map(t -> {
            t.setLikes(t.getLikes() + 1);
            repo.save(t);
            return t.getLikes();
        }).orElseThrow(() -> new RuntimeException("Tournament not found"));
    }
}
