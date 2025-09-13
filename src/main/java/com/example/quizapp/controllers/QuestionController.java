package com.example.quizapp.controllers;

import com.example.quizapp.model.Question;
import com.example.quizapp.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "NONE";
        }
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    @PostMapping
    public ResponseEntity<?> addQuestion(@RequestBody Question question) {
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        Question savedQuestion = questionService.addQuestion(question);
        return ResponseEntity.ok(savedQuestion);
    }

    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        Question updated = questionService.updateQuestion(id, question);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully");
    }
}