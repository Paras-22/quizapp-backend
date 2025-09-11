package com.example.quizapp.services;

import com.example.quizapp.db.QuestionRepository;
import com.example.quizapp.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;


    public Question addQuestion(Question question) {
        return questionRepository.save(question);
    }


    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // READ BY ID
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }

    // UPDATE
    public Question updateQuestion(Long id, Question updatedQuestion) {
        return questionRepository.findById(id).map(existing -> {
            existing.setQuestionText(updatedQuestion.getQuestionText());
            existing.setOptionA(updatedQuestion.getOptionA());
            existing.setOptionB(updatedQuestion.getOptionB());
            existing.setOptionC(updatedQuestion.getOptionC());
            existing.setOptionD(updatedQuestion.getOptionD());
            existing.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
            return questionRepository.save(existing);
        }).orElse(null);
    }

    // DELETE
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}
