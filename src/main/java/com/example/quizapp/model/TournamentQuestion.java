package com.example.quizapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tournament_question")
public class TournamentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private QuizTournament tournament;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private int questionOrder;

    // getters and setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public QuizTournament getTournament() { return tournament; }

    public void setTournament(QuizTournament tournament) { this.tournament = tournament; }

    public Question getQuestion() { return question; }

    public void setQuestion(Question question) { this.question = question; }

    public int getQuestionOrder() { return questionOrder; }

    public void setQuestionOrder(int questionOrder) { this.questionOrder = questionOrder; }
}
