package com.example.quizapp.services;

import com.example.quizapp.db.QuestionRepo;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.model.TournamentQuestion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizTournamentService {

    private final QuizTournamentRepository repo;
    private final QuestionRepo questionRepo;
    private final TournamentQuestionRepository tqRepo;

    public QuizTournamentService(QuizTournamentRepository repo,
                                 QuestionRepo questionRepo,
                                 TournamentQuestionRepository tqRepo) {
        this.repo = repo;
        this.questionRepo = questionRepo;
        this.tqRepo = tqRepo;
    }

    // Create tournament and auto-attach 10 random questions
    public QuizTournament createTournament(QuizTournament quiz) {
        QuizTournament saved = repo.save(quiz);

        // Pick 10 random questions from DB
        List<Question> picked = questionRepo.findRandomQuestions(10);

        int order = 1;
        for (Question q : picked) {
            TournamentQuestion tq = new TournamentQuestion();
            tq.setTournament(saved);
            tq.setQuestion(q);
            tq.setQuestionOrder(order++);
            tqRepo.save(tq);
        }

        return saved;
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

    public int removeLike(Long id) {
        return repo.findById(id).map(t -> {
            t.setLikes(Math.max(0, t.getLikes() - 1));
            repo.save(t);
            return t.getLikes();
        }).orElseThrow(() -> new RuntimeException("Tournament not found"));
    }
}
