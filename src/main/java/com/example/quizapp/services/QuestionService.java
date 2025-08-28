package com.example.quizapp.services;

import com.example.quizapp.db.QuestionRepo;
import com.example.quizapp.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepo questionRepo;

    // CREATE
    public Question addQuestion(Question question) {
        return questionRepo.save(question);
    }

    // READ ALL
    public List<Question> getAllQuestions() {
        return questionRepo.findAll();
    }

    // READ BY ID
    public Optional<Question> getQuestionById(Long id) {
        return questionRepo.findById(id);
    }

    // UPDATE
    public Question updateQuestion(Long id, Question updatedQuestion) {
        return questionRepo.findById(id).map(existing -> {
            existing.setQuestionText(updatedQuestion.getQuestionText());
            existing.setOptionA(updatedQuestion.getOptionA());
            existing.setOptionB(updatedQuestion.getOptionB());
            existing.setOptionC(updatedQuestion.getOptionC());
            existing.setOptionD(updatedQuestion.getOptionD());
            existing.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
            return questionRepo.save(existing);
        }).orElse(null);
    }

    // DELETE
    public void deleteQuestion(Long id) {
        questionRepo.deleteById(id);
    }
}
