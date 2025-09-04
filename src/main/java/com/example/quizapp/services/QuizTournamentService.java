package com.example.quizapp.services;

import com.example.quizapp.db.QuestionRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.model.TournamentQuestion;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class QuizTournamentService {

    private final QuizTournamentRepository repo;
    private final QuestionRepository questionRepo;
    private final TournamentQuestionRepository tqRepo;

    public QuizTournamentService(
            QuizTournamentRepository repo,
            QuestionRepository questionRepo,
            TournamentQuestionRepository tqRepo
    ) {
        this.repo = repo;
        this.questionRepo = questionRepo;
        this.tqRepo = tqRepo;
    }

    // 🔹 Create tournament and auto-fetch 10 questions from OpenTDB
    public QuizTournament createTournament(QuizTournament quiz) {
        QuizTournament saved = repo.save(quiz);

        String url = "https://opentdb.com/api.php?amount=10&type=multiple";
        RestTemplate restTemplate = new RestTemplate();
        Map response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        int order = 1;

        for (Map<String, Object> q : results) {
            Question question = new Question();
            question.setQuestionText((String) q.get("question"));
            question.setCorrectAnswer((String) q.get("correct_answer"));

            List<String> incorrect = (List<String>) q.get("incorrect_answers");
            Collections.shuffle(incorrect); // shuffle to randomize options

            // Map answers into A-D
            question.setOptionA((String) q.get("correct_answer"));
            if (incorrect.size() >= 3) {
                question.setOptionB(incorrect.get(0));
                question.setOptionC(incorrect.get(1));
                question.setOptionD(incorrect.get(2));
            }

            Question savedQ = questionRepo.save(question);

            TournamentQuestion tq = new TournamentQuestion();
            tq.setTournament(saved);
            tq.setQuestion(savedQ);
            tq.setQuestionOrder(order++);
            tqRepo.save(tq);
        }

        return saved;
    }

    public List<QuizTournament> getAllTournaments() {
        return repo.findAll();
    }
}
