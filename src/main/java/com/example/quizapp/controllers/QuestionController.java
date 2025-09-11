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

    private String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String authority = auth.getAuthorities().iterator().next().getAuthority();

        System.out.println("DEBUG - Full authority: " + authority);
        System.out.println("DEBUG - Username: " + auth.getName());

        if (authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }

    @PostMapping
    public ResponseEntity<?> addQuestion(@RequestBody Question question) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        Question savedQuestion = questionService.addQuestion(question);
        return ResponseEntity.ok(savedQuestion);
    }

    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable Long id) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
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
        if (!"ADMIN".equals(getRole())) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully");
    }
}
