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
    private QuestionRepository questionRepository; // Here I add repository injection for question operations

    public Question addQuestion(Question question) {
        // Here I add logic to save a new question
        return questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        // Here I add logic to fetch all questions
        return questionRepository.findAll();
    }

    public Optional<Question> getQuestionById(Long id) {
        // Here I add logic to fetch a question by ID
        return questionRepository.findById(id);
    }

    public Question updateQuestion(Long id, Question updatedQuestion) {
        // Here I add logic to update an existing question
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

    public void deleteQuestion(Long id) {
        // Here I add logic to delete a question by ID
        questionRepository.deleteById(id);
    }
}
