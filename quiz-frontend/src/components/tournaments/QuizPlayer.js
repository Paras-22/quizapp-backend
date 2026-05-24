// QuizPlayer.js - Component for playing a quiz tournament with countdown timer

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { apiService } from '../../services/api';
import { Clock, CheckCircle, XCircle, ArrowRight, Trophy } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';

const TIMER_SECONDS = 30;

const QuizPlayer = () => {
  // Get tournament ID from URL and attempt ID from navigation state
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const attemptId = location.state?.attemptId;

  // State variables for quiz logic
  const [questions, setQuestions] = useState([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [isFinished, setIsFinished] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [score, setScore] = useState(0);

  // countdown timer that resets on each question
  const [timeLeft, setTimeLeft] = useState(TIMER_SECONDS);
  const [timerActive, setTimerActive] = useState(false);
  const [timedOut, setTimedOut] = useState(false);

  // Load questions when component mounts
  useEffect(() => {
    if (!attemptId) {
      navigate('/dashboard');
      return;
    }
    loadQuestions();
  }, [tournamentId, attemptId]);

  // Reset timer every time question changes
  useEffect(() => {
    if (questions.length > 0 && !isFinished) {
      setTimeLeft(TIMER_SECONDS);
      setTimerActive(true);
      setTimedOut(false);
    }
  }, [currentQuestionIndex, questions.length]);

  // Countdown tick — stops when feedback is showing or timer is paused
  useEffect(() => {
    if (!timerActive || feedback !== null) return;

    if (timeLeft === 0) {
      handleTimeUp();
      return;
    }

    const interval = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(interval);
  }, [timeLeft, timerActive, feedback]);

  // What happens when timer hits zero
  const handleTimeUp = useCallback(() => {
    setTimerActive(false);
    setTimedOut(true);

    // Wait 1.5 seconds to show Time's up message then move on
    setTimeout(() => {
      if (currentQuestionIndex < questions.length - 1) {
        setCurrentQuestionIndex(prev => prev + 1);
        setFeedback(null);
        setTimedOut(false);
      } else {
        finishQuiz();
      }
    }, 1500);
  }, [currentQuestionIndex, questions.length]);

  // Fetch tournament questions from API
  const loadQuestions = async () => {
    try {
      const data = await apiService.getTournamentQuestions(tournamentId);
      setQuestions(data);
      setTimerActive(true);
    } catch (error) {
      console.error('Error loading questions:', error);
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  // Handle answer selection
  const handleAnswerSelect = (selectedAnswer) => {
    if (timedOut) return;
    setAnswers({
      ...answers,
      [currentQuestionIndex]: selectedAnswer
    });
  };

  // Submit selected answer to API and show feedback
  const handleSubmitAnswer = async () => {
    const selectedAnswer = answers[currentQuestionIndex];
    if (!selectedAnswer) return;

    // Stop timer when answer is submitted
    setTimerActive(false);
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

      // Automatically move to next question or finish quiz
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
      setTimerActive(true); // resume timer if submission failed
    } finally {
      setSubmitting(false);
    }
  };

  // Mark quiz as finished
  const finishQuiz = async () => {
    setTimerActive(false);
    try {
      await apiService.finishTournament(attemptId);
      setIsFinished(true);
    } catch (error) {
      console.error('Error finishing tournament:', error);
    }
  };

  // timer display with progress bar — colour changes based on time left
  const getTimerColor = () => {
    if (timeLeft > 15) return 'text-green-600';
    if (timeLeft > 7) return 'text-yellow-500';
    return 'text-red-600';
  };

  const getTimerBg = () => {
    if (timeLeft > 15) return 'bg-green-50 border-green-200';
    if (timeLeft > 7) return 'bg-yellow-50 border-yellow-200';
    return 'bg-red-50 border-red-200';
  };

  const getBarColor = () => {
    if (timeLeft > 15) return 'bg-green-500';
    if (timeLeft > 7) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  // Show loading spinner while fetching questions
  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  // Show completion screen when quiz is finished
  if (isFinished) {
    return (
      <div className="max-w-2xl mx-auto px-4">
        <Card className="text-center">
          <Trophy className="h-16 w-16 text-yellow-500 mx-auto mb-4" />
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Quiz Completed!</h1>
          <p className="text-xl text-gray-600 mb-6">
            Your Score:{' '}
            <span className="font-semibold text-blue-600">
              {score}/{questions.length}
            </span>
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

  // Get current question and selected answer
  const currentQuestion = questions[currentQuestionIndex];
  const selectedAnswer = answers[currentQuestionIndex];

  return (
    <div className="max-w-4xl mx-auto px-4">
      {/* Quiz header with progress and score */}
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

        {/* Progress bar */}
        <div className="w-full bg-gray-200 rounded-full h-2 mb-4">
          <div
            className="bg-blue-600 h-2 rounded-full transition-all duration-300"
            style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }}
          />
        </div>

        {/* timer display with progress bar */}
        <div className={`flex items-center justify-between p-3 rounded-lg border ${getTimerBg()}`}>
          <div className="flex items-center gap-2">
            <Clock className={`h-5 w-5 ${getTimerColor()}`} />
            <span className={`font-bold text-lg ${getTimerColor()}`}>
              {timedOut ? "Time's up!" : `${timeLeft}s`}
            </span>
          </div>
          <div className="flex-1 mx-4 bg-gray-200 rounded-full h-2">
            <div
              className={`h-2 rounded-full transition-all duration-1000 ${getBarColor()}`}
              style={{ width: `${(timeLeft / TIMER_SECONDS) * 100}%` }}
            />
          </div>
          <span className="text-xs text-gray-500">{TIMER_SECONDS}s limit</span>
        </div>
      </div>

      {/* Question card */}
      {currentQuestion && (
        <Card>
          {/* Time's up banner */}
          {timedOut && (
            <div className="mb-4 p-3 bg-red-100 border border-red-300 rounded-lg text-center">
              <p className="text-red-700 font-semibold">
                ⏰ Time's up! Moving to the next question...
              </p>
            </div>
          )}

          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              {currentQuestion.question.questionText}
            </h2>

            {/* Answer options */}
            <div className="space-y-3">
              {['A', 'B', 'C', 'D'].map(option => {
                const optionText = currentQuestion.question[`option${option}`];
                const isSelected = selectedAnswer === option;

                return (
                  <button
                    key={option}
                    onClick={() => handleAnswerSelect(option)}
                    disabled={feedback !== null || submitting || timedOut}
                    className={`w-full p-4 text-left border-2 rounded-lg transition-all ${
                      isSelected
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300 bg-white'
                    } ${
                      feedback !== null || timedOut
                        ? 'cursor-not-allowed opacity-60'
                        : 'cursor-pointer'
                    }`}
                  >
                    <span className="font-medium mr-3">{option}.</span>
                    {optionText}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Feedback after answer submission */}
          {feedback && (
            <div
              className={`mb-6 p-4 rounded-lg ${
                feedback.isCorrect
                  ? 'bg-green-100 text-green-800'
                  : 'bg-red-100 text-red-800'
              }`}
            >
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
                  The correct answer was:{' '}
                  <strong>{feedback.correctAnswer}</strong>
                </p>
              )}
            </div>
          )}

          {/* Submit button — hidden when timed out or feedback showing */}
          {!feedback && !timedOut && (
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