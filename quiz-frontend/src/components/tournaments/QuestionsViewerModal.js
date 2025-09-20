// QuestionsViewerModal.js - Modal to view tournament questions
import React, { useState, useEffect } from 'react';
import { X, CheckCircle, XCircle } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import { apiService } from '../../services/api';

const QuestionsViewerModal = ({ tournament, onClose, isOpen }) => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen && tournament) {
      loadQuestions();
    }
  }, [isOpen, tournament]);

  const loadQuestions = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await apiService.getTournamentQuestions(tournament.id);
      setQuestions(data);
    } catch (err) {
      setError('Failed to load questions. Please try again.');
      console.error('Error loading questions:', err);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-96 overflow-y-auto">
        <div className="sticky top-0 bg-white border-b p-6 flex justify-between items-center">
          <div>
            <h2 className="text-xl font-semibold">Tournament Questions</h2>
            <p className="text-gray-600">{tournament?.name}</p>
          </div>
          <Button variant="outline" size="sm" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>

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

          {!loading && !error && questions.length === 0 && (
            <div className="text-center py-8">
              <p className="text-gray-500">No questions available for this tournament</p>
            </div>
          )}

          {!loading && questions.length > 0 && (
            <div className="space-y-6">
              {questions.map((tq, index) => (
                <Card key={tq.id} className="p-4">
                  <div className="mb-4">
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="text-lg font-medium">Question {index + 1}</h3>
                      <span className="text-sm text-gray-500">Order: {tq.questionOrder}</span>
                    </div>
                    <p className="text-gray-800 mb-4">{tq.question.questionText}</p>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
                    {['A', 'B', 'C', 'D'].map(option => {
                      const optionText = tq.question[`option${option}`];
                      const isCorrect = optionText === tq.question.correctAnswer;
                      
                      return (
                        <div 
                          key={option}
                          className={`p-3 rounded-lg border-2 ${
                            isCorrect 
                              ? 'border-green-500 bg-green-50' 
                              : 'border-gray-200 bg-gray-50'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <div className="flex items-center">
                              <span className="font-medium mr-2">{option}.</span>
                              <span>{optionText}</span>
                            </div>
                            {isCorrect && (
                              <CheckCircle className="h-4 w-4 text-green-600" />
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>

                  <div className="bg-green-100 border border-green-400 p-3 rounded-lg">
                    <div className="flex items-center">
                      <CheckCircle className="h-4 w-4 text-green-600 mr-2" />
                      <span className="font-medium text-green-800">Correct Answer: </span>
                      <span className="text-green-700">{tq.question.correctAnswer}</span>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        <div className="sticky bottom-0 bg-white border-t p-4">
          <div className="flex justify-between items-center">
            <span className="text-sm text-gray-600">
              {questions.length} question{questions.length !== 1 ? 's' : ''} total
            </span>
            <Button onClick={onClose}>Close</Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuestionsViewerModal;