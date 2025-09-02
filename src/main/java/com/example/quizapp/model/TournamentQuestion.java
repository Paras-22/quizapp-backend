package com.example.quizapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "tournament_questions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TournamentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id")
    @JsonIgnore
    private QuizTournament tournament;

    @Column(nullable = false, length = 500)
    private String questionText;

    @Column(nullable = false) private String optionA;
    @Column(nullable = false) private String optionB;
    @Column(nullable = false) private String optionC;
    @Column(nullable = false) private String optionD;

    @Column(nullable = false)
    private String correctAnswer;


    public Long getId() { return id; }
    public QuizTournament getTournament() { return tournament; }
    public void setTournament(QuizTournament tournament) { this.tournament = tournament; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
}
