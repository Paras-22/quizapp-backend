// QuizReviewModal.js - Larger version for better player experience
import React, { useState, useEffect } from 'react';
import { X, CheckCircle, XCircle, Trophy, Clock, Target } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import { apiService } from '../../services/api';

const QuizReviewModal = ({ attempt, isOpen, onClose }) => {
  const [reviewData, setReviewData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  useEffect(() => {
    if (isOpen && attempt) {
      loadReviewData();
    }
  }, [isOpen, attempt]);

  const loadReviewData = async () => {
    setLoading(true);
    setError('');
    try {
      // Get tournament questions and player answers
      const [questions, answers] = await Promise.all([
        apiService.getTournamentQuestions(attempt.tournament.id),
        apiService.getAttemptAnswers(attempt.id)
      ]);
      
      // Combine questions with player answers
      const reviewData = questions.map(tq => {
        const playerAnswer = answers.find(ans => ans.question.id === tq.question.id);
        return {
          tournamentQuestion: tq,
          question: tq.question,
          playerAnswer: playerAnswer
        };
      });
      
      setReviewData(reviewData);
    } catch (err) {
      setError('Failed to load quiz review. Please try again.');
      console.error('Error loading review data:', err);
    } finally {
      setLoading(false);
    }
  };

  const getScoreColor = (score, total) => {
    const percentage = (score / total) * 100;
    if (percentage >= 80) return 'text-green-600';
    if (percentage >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getScoreBadgeColor = (score, total) => {
    const percentage = (score / total) * 100;
    if (percentage >= 80) return 'bg-green-100 text-green-800 border-green-200';
    if (percentage >= 60) return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    return 'bg-red-100 text-red-800 border-red-200';
  };

  const handlePreviousQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };

  const handleNextQuestion = () => {
    if (currentQuestionIndex < reviewData.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg w-full max-w-7xl h-full max-h-screen overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex-shrink-0 bg-white border-b p-6 flex justify-between items-center">
          <div>
            <h2 className="text-2xl font-semibold">Quiz Review</h2>
            <p className="text-gray-600 text-lg">{attempt?.tournament?.name}</p>
          </div>
          <Button variant="outline" size="sm" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>

        {/* Summary Section */}
        {attempt && (
          <div className="flex-shrink-0 p-6 bg-gradient-to-r from-blue-50 to-indigo-50 border-b">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="flex items-center space-x-3 bg-white p-4 rounded-lg shadow-sm">
                <Trophy className="h-8 w-8 text-yellow-600" />
                <div>
                  <div className={`text-2xl font-bold ${getScoreColor(attempt.score, 10)}`}>
                    {attempt.score}/10
                  </div>
                  <div className="text-sm text-gray-600">Final Score</div>
                </div>
              </div>
              
              <div className="flex items-center space-x-3 bg-white p-4 rounded-lg shadow-sm">
                <Target className="h-8 w-8 text-blue-600" />
                <div>
                  <div className="text-2xl font-bold">
                    {Math.round((attempt.score / 10) * 100)}%
                  </div>
                  <div className="text-sm text-gray-600">Percentage</div>
                </div>
              </div>
              
              <div className="flex items-center space-x-3 bg-white p-4 rounded-lg shadow-sm">
                <Clock className="h-8 w-8 text-purple-600" />
                <div>
                  <div className="text-2xl font-bold">
                    {new Date(attempt.completedAt).toLocaleDateString()}
                  </div>
                  <div className="text-sm text-gray-600">Completed</div>
                </div>
              </div>
              
              <div className="flex items-center justify-center bg-white p-4 rounded-lg shadow-sm">
                <span className={`px-4 py-2 text-lg font-semibold rounded-full border ${getScoreBadgeColor(attempt.score, 10)}`}>
                  {attempt.score >= attempt.tournament.minPassingScore ? 'PASSED' : 'FAILED'}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {loading && (
            <div className="flex justify-center items-center h-64">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
          )}

          {error && (
            <div className="p-6 bg-red-100 border border-red-400 text-red-700 rounded-lg mb-6 text-center">
              <p className="text-lg">{error}</p>
            </div>
          )}

          {!loading && !error && reviewData && reviewData.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-500 text-lg">No review data available</p>
            </div>
          )}

          {!loading && reviewData && reviewData.length > 0 && (
            <div className="max-w-5xl mx-auto">
              {/* Question Navigation */}
              <div className="flex justify-between items-center mb-8">
                <h3 className="text-xl font-semibold">
                  Question {currentQuestionIndex + 1} of {reviewData.length}
                </h3>
                <div className="flex space-x-2">
                  <Button
                    onClick={handlePreviousQuestion}
                    disabled={currentQuestionIndex === 0}
                    variant="outline"
                  >
                    Previous
                  </Button>
                  <Button
                    onClick={handleNextQuestion}
                    disabled={currentQuestionIndex === reviewData.length - 1}
                    variant="outline"
                  >
                    Next
                  </Button>
                </div>
              </div>

              {/* Current Question Display */}
              {(() => {
                const item = reviewData[currentQuestionIndex];
                const { question, playerAnswer } = item;
                const isCorrect = playerAnswer?.correct || false;
                const selectedOption = playerAnswer?.selectedAnswer;
                
                return (
                  <Card className="p-8 mb-6">
                    {/* Question Header */}
                    <div className="flex items-center justify-between mb-6">
                      <h4 className="text-2xl font-semibold">Question {currentQuestionIndex + 1}</h4>
                      <div className="flex items-center space-x-3">
                        {isCorrect ? (
                          <CheckCircle className="h-8 w-8 text-green-600" />
                        ) : (
                          <XCircle className="h-8 w-8 text-red-600" />
                        )}
                        <span className={`text-xl font-semibold ${isCorrect ? 'text-green-600' : 'text-red-600'}`}>
                          {isCorrect ? 'Correct' : 'Incorrect'}
                        </span>
                      </div>
                    </div>

                    {/* Question Text */}
                    <div className="mb-8">
                      <p className="text-xl text-gray-800 leading-relaxed">{question.questionText}</p>
                    </div>

                    {/* Answer Options */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
                      {['A', 'B', 'C', 'D'].map(option => {
                        const optionText = question[`option${option}`];
                        const isPlayerAnswer = selectedOption === option;
                        const isCorrectAnswer = optionText === question.correctAnswer;
                        
                        let optionClass = 'p-4 rounded-lg border-2 transition-all ';
                        if (isCorrectAnswer) {
                          optionClass += 'border-green-500 bg-green-50';
                        } else if (isPlayerAnswer && !isCorrectAnswer) {
                          optionClass += 'border-red-500 bg-red-50';
                        } else {
                          optionClass += 'border-gray-200 bg-gray-50';
                        }
                        
                        return (
                          <div key={option} className={optionClass}>
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <span className="font-bold text-lg mr-3 w-8 h-8 flex items-center justify-center rounded-full bg-white border-2">
                                  {option}
                                </span>
                                <span className="text-lg">{optionText}</span>
                              </div>
                              <div className="flex items-center space-x-2">
                                {isPlayerAnswer && (
                                  <span className="text-sm px-3 py-1 bg-blue-100 text-blue-800 rounded-full font-medium">
                                    Your Answer
                                  </span>
                                )}
                                {isCorrectAnswer && (
                                  <CheckCircle className="h-6 w-6 text-green-600" />
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>

                    {/* Result Summary */}
                    <div className={`p-6 rounded-lg ${isCorrect ? 'bg-green-100 border-2 border-green-200' : 'bg-red-100 border-2 border-red-200'}`}>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center">
                          {isCorrect ? (
                            <CheckCircle className="h-6 w-6 text-green-600 mr-3" />
                          ) : (
                            <XCircle className="h-6 w-6 text-red-600 mr-3" />
                          )}
                          <span className={`font-semibold text-lg ${isCorrect ? 'text-green-800' : 'text-red-800'}`}>
                            {isCorrect ? 'Excellent! You got this right!' : 'This answer was incorrect.'}
                          </span>
                        </div>
                        {!isCorrect && (
                          <span className="text-lg text-red-700 font-medium">
                            Correct answer: {question.correctAnswer}
                          </span>
                        )}
                      </div>
                    </div>
                  </Card>
                );
              })()}

              {/* Question Progress Indicators */}
              <div className="flex justify-center space-x-2 mb-6">
                {reviewData.map((_, index) => {
                  const item = reviewData[index];
                  const isCorrect = item.playerAnswer?.correct || false;
                  return (
                    <button
                      key={index}
                      onClick={() => setCurrentQuestionIndex(index)}
                      className={`w-12 h-12 rounded-full border-2 font-semibold text-sm transition-all ${
                        index === currentQuestionIndex
                          ? 'border-blue-500 bg-blue-50 text-blue-700'
                          : isCorrect
                          ? 'border-green-500 bg-green-50 text-green-700'
                          : 'border-red-500 bg-red-50 text-red-700'
                      }`}
                    >
                      {index + 1}
                    </button>
                  );
                })}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex-shrink-0 bg-white border-t p-6">
          <div className="flex justify-between items-center">
            <div className="text-lg text-gray-600">
              {reviewData ? `Reviewed ${reviewData.length} questions` : ''}
            </div>
            <Button onClick={onClose} size="lg">Close Review</Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuizReviewModal;