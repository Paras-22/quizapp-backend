// src/components/tournaments/QuizPlayer.js
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { apiService } from '../../services/api';
import { Clock, CheckCircle, XCircle, ArrowRight, Trophy } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';

const QuizPlayer = () => {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const attemptId = location.state?.attemptId;

  const [questions, setQuestions] = useState([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [isFinished, setIsFinished] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [score, setScore] = useState(0);

  useEffect(() => {
    if (!attemptId) {
      navigate('/dashboard');
      return;
    }
    loadQuestions();
  }, [tournamentId, attemptId]);

  const loadQuestions = async () => {
    try {
      const data = await apiService.getTournamentQuestions(tournamentId);
      setQuestions(data);
    } catch (error) {
      console.error('Error loading questions:', error);
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (selectedAnswer) => {
    setAnswers({
      ...answers,
      [currentQuestionIndex]: selectedAnswer
    });
  };

  const handleSubmitAnswer = async () => {
    const selectedAnswer = answers[currentQuestionIndex];
    if (!selectedAnswer) return;

    setSubmitting(true);
    try {
      const result = await apiService.submitAnswer({
        attemptId,
        tqId: questions[currentQuestionIndex].id,
        selectedAnswer
      });

      setFeedback(result);
      if (result.isCorrect) {
        setScore(prev => prev + 1);
      }

      // Auto-advance after showing feedback
      setTimeout(() => {
        if (currentQuestionIndex < questions.length - 1) {
          setCurrentQuestionIndex(prev => prev + 1);
          setFeedback(null);
        } else {
          finishQuiz();
        }
      }, 2000);
    } catch (error) {
      console.error('Error submitting answer:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const finishQuiz = async () => {
    try {
      await apiService.finishTournament(attemptId);
      setIsFinished(true);
    } catch (error) {
      console.error('Error finishing tournament:', error);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (isFinished) {
    return (
      <div className="max-w-2xl mx-auto px-4">
        <Card className="text-center">
          <Trophy className="h-16 w-16 text-yellow-500 mx-auto mb-4" />
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Quiz Completed!</h1>
          <p className="text-xl text-gray-600 mb-6">
            Your Score: <span className="font-semibold text-blue-600">{score}/{questions.length}</span>
          </p>
          <p className="text-gray-500 mb-8">
            {score >= 7 ? 'Congratulations! Great job!' : 'Good effort! Keep practicing!'}
          </p>
          <Button onClick={() => navigate('/dashboard')}>
            Return to Dashboard
          </Button>
        </Card>
      </div>
    );
  }

  const currentQuestion = questions[currentQuestionIndex];
  const selectedAnswer = answers[currentQuestionIndex];

  return (
    <div className="max-w-4xl mx-auto px-4">
      <div className="mb-6">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-2xl font-bold text-gray-900">Quiz Tournament</h1>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-600">
              Question {currentQuestionIndex + 1} of {questions.length}
            </span>
            <div className="flex items-center">
              <Trophy className="h-4 w-4 text-yellow-500 mr-1" />
              <span className="text-sm font-medium">{score}</span>
            </div>
          </div>
        </div>
        
        <div className="w-full bg-gray-200 rounded-full h-2">
          <div 
            className="bg-blue-600 h-2 rounded-full transition-all duration-300"
            style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }}
          />
        </div>
      </div>

      {currentQuestion && (
        <Card>
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              {currentQuestion.question.questionText}
            </h2>
            
            <div className="space-y-3">
              {['A', 'B', 'C', 'D'].map(option => {
                const optionText = currentQuestion.question[`option${option}`];
                const isSelected = selectedAnswer === option;
                
                return (
                  <button
                    key={option}
                    onClick={() => handleAnswerSelect(option)}
                    disabled={feedback !== null || submitting}
                    className={`w-full p-4 text-left border-2 rounded-lg transition-all ${
                      isSelected
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300 bg-white'
                    } ${feedback !== null ? 'cursor-not-allowed' : 'cursor-pointer'}`}
                  >
                    <span className="font-medium mr-3">{option}.</span>
                    {optionText}
                  </button>
                );
              })}
            </div>
          </div>

          {feedback && (
            <div className={`mb-6 p-4 rounded-lg ${
              feedback.isCorrect ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}>
              <div className="flex items-center mb-2">
                {feedback.isCorrect ? (
                  <CheckCircle className="h-5 w-5 mr-2" />
                ) : (
                  <XCircle className="h-5 w-5 mr-2" />
                )}
                <span className="font-medium">{feedback.feedback}</span>
              </div>
              {!feedback.isCorrect && (
                <p className="text-sm">
                  The correct answer was: <strong>{feedback.correctAnswer}</strong>
                </p>
              )}
            </div>
          )}

          {!feedback && (
            <Button
              onClick={handleSubmitAnswer}
              disabled={!selectedAnswer || submitting}
              className="w-full"
            >
              {submitting ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Submitting...
                </div>
              ) : (
                <>
                  Submit Answer
                  <ArrowRight className="h-4 w-4 ml-2" />
                </>
              )}
            </Button>
          )}
        </Card>
      )}
    </div>
  );
};

export default QuizPlayer;