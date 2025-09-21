// QuizReviewModal.js - Modal for players to review their completed quizzes
import React, { useState, useEffect } from 'react';
import { X, CheckCircle, XCircle, Trophy, Clock, Target } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import { apiService } from '../../services/api';

const QuizReviewModal = ({ attempt, isOpen, onClose }) => {
  const [reviewData, setReviewData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-96 overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b p-6 flex justify-between items-center">
          <div>
            <h2 className="text-xl font-semibold">Quiz Review</h2>
            <p className="text-gray-600">{attempt?.tournament?.name}</p>
          </div>
          <Button variant="outline" size="sm" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Summary Section */}
        {attempt && (
          <div className="p-6 bg-gray-50 border-b">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="flex items-center space-x-2">
                <Trophy className="h-5 w-5 text-yellow-600" />
                <div>
                  <div className={`text-lg font-semibold ${getScoreColor(attempt.score, 10)}`}>
                    {attempt.score}/10
                  </div>
                  <div className="text-xs text-gray-600">Final Score</div>
                </div>
              </div>
              
              <div className="flex items-center space-x-2">
                <Target className="h-5 w-5 text-blue-600" />
                <div>
                  <div className="text-lg font-semibold">
                    {Math.round((attempt.score / 10) * 100)}%
                  </div>
                  <div className="text-xs text-gray-600">Percentage</div>
                </div>
              </div>
              
              <div className="flex items-center space-x-2">
                <Clock className="h-5 w-5 text-purple-600" />
                <div>
                  <div className="text-lg font-semibold">
                    {new Date(attempt.completedAt).toLocaleDateString()}
                  </div>
                  <div className="text-xs text-gray-600">Completed</div>
                </div>
              </div>
              
              <div className="flex items-center justify-center">
                <span className={`px-3 py-1 text-sm rounded-full border ${getScoreBadgeColor(attempt.score, 10)}`}>
                  {attempt.score >= attempt.tournament.minPassingScore ? 'PASSED' : 'FAILED'}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Content */}
        <div className="p-6">
          {loading && (
            <div className="flex justify-center items-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          )}

          {error && (
            <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg mb-4">
              {error}
            </div>
          )}

          {!loading && !error && reviewData && reviewData.length === 0 && (
            <div className="text-center py-8">
              <p className="text-gray-500">No review data available</p>
            </div>
          )}

          {!loading && reviewData && reviewData.length > 0 && (
            <div className="space-y-6">
              {reviewData.map((item, index) => {
                const { question, playerAnswer, tournamentQuestion } = item;
                const isCorrect = playerAnswer?.correct || false;
                const selectedOption = playerAnswer?.selectedAnswer;
                
                return (
                  <Card key={question.id} className="p-6">
                    {/* Question Header */}
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-lg font-medium">Question {index + 1}</h3>
                      <div className="flex items-center space-x-2">
                        {isCorrect ? (
                          <CheckCircle className="h-5 w-5 text-green-600" />
                        ) : (
                          <XCircle className="h-5 w-5 text-red-600" />
                        )}
                        <span className={`text-sm font-medium ${isCorrect ? 'text-green-600' : 'text-red-600'}`}>
                          {isCorrect ? 'Correct' : 'Incorrect'}
                        </span>
                      </div>
                    </div>

                    {/* Question Text */}
                    <p className="text-gray-800 mb-4">{question.questionText}</p>

                    {/* Answer Options */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
                      {['A', 'B', 'C', 'D'].map(option => {
                        const optionText = question[`option${option}`];
                        const isPlayerAnswer = selectedOption === option;
                        const isCorrectAnswer = optionText === question.correctAnswer;
                        
                        let optionClass = 'p-3 rounded-lg border-2 ';
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
                                <span className="font-medium mr-2">{option}.</span>
                                <span>{optionText}</span>
                              </div>
                              <div className="flex items-center space-x-1">
                                {isPlayerAnswer && (
                                  <span className="text-xs px-2 py-1 bg-blue-100 text-blue-800 rounded">
                                    Your Answer
                                  </span>
                                )}
                                {isCorrectAnswer && (
                                  <CheckCircle className="h-4 w-4 text-green-600" />
                                )}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>

                    {/* Result Summary */}
                    <div className={`p-3 rounded-lg ${isCorrect ? 'bg-green-100 border border-green-200' : 'bg-red-100 border border-red-200'}`}>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center">
                          {isCorrect ? (
                            <CheckCircle className="h-4 w-4 text-green-600 mr-2" />
                          ) : (
                            <XCircle className="h-4 w-4 text-red-600 mr-2" />
                          )}
                          <span className={`font-medium ${isCorrect ? 'text-green-800' : 'text-red-800'}`}>
                            {isCorrect ? 'You got this right!' : 'You got this wrong.'}
                          </span>
                        </div>
                        {!isCorrect && (
                          <span className="text-sm text-red-700">
                            Correct answer: {question.correctAnswer}
                          </span>
                        )}
                      </div>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="sticky bottom-0 bg-white border-t p-4">
          <div className="flex justify-between items-center">
            <div className="text-sm text-gray-600">
              {reviewData ? `${reviewData.length} questions reviewed` : ''}
            </div>
            <Button onClick={onClose}>Close Review</Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuizReviewModal;