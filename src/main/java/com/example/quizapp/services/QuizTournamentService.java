package com.example.quizapp.services;

import com.example.quizapp.db.QuestionRepository;
import com.example.quizapp.db.QuizTournamentRepository;
import com.example.quizapp.db.TournamentQuestionRepository;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.QuizTournament;
import com.example.quizapp.model.TournamentQuestion;
import org.springframework.http.ResponseEntity;
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
        System.out.println("✅ Saved tournament with ID: " + saved.getId());

        try {
            String url = "https://opentdb.com/api.php?amount=10&type=multiple";
            RestTemplate restTemplate = new RestTemplate();

            System.out.println("🌐 Calling OpenTDB API...");
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                System.out.println("❌ API call failed: " + responseEntity.getStatusCode());
                return saved;
            }

            Map<String, Object> response = responseEntity.getBody();

            if (response == null) {
                System.out.println("❌ API response is null");
                return saved;
            }

            System.out.println("✅ API Response keys: " + response.keySet());

            // Check response structure
            if (!response.containsKey("results")) {
                System.out.println("❌ No 'results' in response: " + response);
                return saved;
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

            if (results == null) {
                System.out.println("❌ Results is null");
                return saved;
            }

            System.out.println("📊 Found " + results.size() + " questions");

            if (results.isEmpty()) {
                System.out.println("⚠️ Results list is empty");
                return saved;
            }

            int successCount = 0;
            int order = 1;

            for (Map<String, Object> q : results) {
                try {
                    System.out.println("--- Processing Question " + order + " ---");

                    Question question = new Question();

                    // Extract and decode question text
                    String questionText = (String) q.get("question");
                    if (questionText != null) {
                        questionText = decodeHtmlEntities(questionText);
                        System.out.println("📝 Question: " + questionText);
                    }
                    question.setQuestionText(questionText);

                    // Extract and decode correct answer
                    String correctAnswer = (String) q.get("correct_answer");
                    if (correctAnswer != null) {
                        correctAnswer = decodeHtmlEntities(correctAnswer);
                        System.out.println("✅ Correct: " + correctAnswer);
                    }
                    question.setCorrectAnswer(correctAnswer);

                    // Extract and process incorrect answers
                    List<String> incorrect = (List<String>) q.get("incorrect_answers");
                    if (incorrect != null) {
                        System.out.println("❌ Incorrect: " + incorrect);
                        Collections.shuffle(incorrect);

                        question.setOptionA(correctAnswer);

                        if (incorrect.size() >= 3) {
                            question.setOptionB(decodeHtmlEntities(incorrect.get(0)));
                            question.setOptionC(decodeHtmlEntities(incorrect.get(1)));
                            question.setOptionD(decodeHtmlEntities(incorrect.get(2)));
                        }
                    }

                    // Save question
                    Question savedQ = questionRepo.save(question);
                    System.out.println("💾 Saved Question ID: " + savedQ.getId());

                    // Create tournament-question link
                    TournamentQuestion tq = new TournamentQuestion();
                    tq.setTournament(saved);
                    tq.setQuestion(savedQ);
                    tq.setQuestionOrder(order);

                    TournamentQuestion savedTq = tqRepo.save(tq);
                    System.out.println("🔗 Saved TournamentQuestion ID: " + savedTq.getId());

                    successCount++;
                    order++;

                } catch (Exception e) {
                    System.out.println("❌ Error processing question: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("🎉 Successfully processed " + successCount + "/" + results.size() + " questions");

            // Verify the links
            List<TournamentQuestion> verification = tqRepo.findByTournamentId(saved.getId());
            System.out.println("🔍 Verification: " + verification.size() + " links found for tournament " + saved.getId());

        } catch (Exception e) {
            System.out.println("💥 Critical error in createTournament: " + e.getMessage());
            e.printStackTrace();
        }

        return saved;
    }

    // Helper method to decode HTML entities
    private String decodeHtmlEntities(String text) {
        if (text == null) return null;
        return text.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&uuml;", "ü")
                .replace("&ouml;", "ö")
                .replace("&auml;", "ä")
                .replace("&eacute;", "é")
                .replace("&iacute;", "í")
                .replace("&oacute;", "ó")
                .replace("&uacute;", "ú")
                .replace("&ntilde;", "ñ")
                .replace("&ldquo;", "\"")
                .replace("&rdquo;", "\"");
    }

    public List<QuizTournament> getAllTournaments() {
        return repo.findAll();
    }

    public QuizTournament updateTournament(Long id, QuizTournament updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setStartDate(updated.getStartDate());
                    existing.setEndDate(updated.getEndDate());
                    return repo.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));
    }

    public void deleteTournament(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new RuntimeException("Tournament not found with id: " + id);
        }
    }

    public int addLike(Long id) {
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        tournament.setLikes(tournament.getLikes() + 1);
        repo.save(tournament);
        return tournament.getLikes();
    }

    public int removeLike(Long id) {
        QuizTournament tournament = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        tournament.setLikes(Math.max(0, tournament.getLikes() - 1));
        repo.save(tournament);
        return tournament.getLikes();
    }
}