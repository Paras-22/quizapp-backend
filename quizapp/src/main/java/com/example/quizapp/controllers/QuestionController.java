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
    private QuestionService questionService; // Here I add service injection for question operations

    private String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "NONE";
        }

        String authority = auth.getAuthorities().iterator().next().getAuthority();

        // Remove ROLE_ prefix if present (Spring Security adds this automatically)
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5); // Returns "ADMIN" instead of "ROLE_ADMIN"
        }
        return authority;
    }
    @PostMapping
    public ResponseEntity<?> addQuestion(@RequestBody Question question) {
        // Here I add role check to restrict access to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        // Here I add logic to save a new question
        Question savedQuestion = questionService.addQuestion(question);
        return ResponseEntity.ok(savedQuestion);
    }

    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        // Here I add role check to restrict access to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        // Here I add logic to fetch all questions
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        // Here I add role check to restrict access to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        // Here I add logic to fetch a question by ID
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        // Here I add role check to restrict access to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        // Here I add logic to update a question
        Question updated = questionService.updateQuestion(id, question);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        // Here I add role check to restrict access to admins
        String role = getCurrentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only. Current role: " + role);
        }
        // Here I add logic to delete a question by ID
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully");
    }
}
