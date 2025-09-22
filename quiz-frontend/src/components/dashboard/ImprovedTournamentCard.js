// Simple improved tournament cards for better player experience
import React from 'react';
import { Calendar, Star, Play, Trophy, Clock, Target } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';

const ImprovedTournamentCard = ({ 
  tournament, 
  onStart, 
  onLike, 
  userAttempts = [] 
}) => {
  const hasAttempted = userAttempts.some(
    attempt => attempt.tournament.id === tournament.id
  );
  
  const getDifficultyColor = (difficulty) => {
    switch (difficulty?.toLowerCase()) {
      case 'easy': return 'bg-green-100 text-green-700 border-green-200';
      case 'medium': return 'bg-yellow-100 text-yellow-700 border-yellow-200';
      case 'hard': return 'bg-red-100 text-red-700 border-red-200';
      default: return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const getDaysLeft = () => {
    const endDate = new Date(tournament.endDate);
    const now = new Date();
    const diffTime = endDate - now;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const daysLeft = getDaysLeft();

  return (
    <Card className="h-full hover:shadow-lg transition-all duration-200 border-gray-200">
      {/* Header */}
      <div className="p-6 pb-4">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-xl font-semibold text-gray-900 line-clamp-2">
            {tournament.name}
          </h3>
          <div className="flex items-center space-x-1 text-gray-500">
            <Star className="h-4 w-4 text-yellow-500" />
            <span className="text-sm">{tournament.likes || 0}</span>
          </div>
        </div>

        <div className="flex items-center space-x-3 mb-4">
          <span className="text-lg font-medium text-blue-600">{tournament.category}</span>
          <span className={`px-3 py-1 rounded-full text-sm font-medium border ${getDifficultyColor(tournament.difficulty)}`}>
            {tournament.difficulty}
          </span>
        </div>
      </div>

      {/* Info Section */}
      <div className="px-6 pb-4 space-y-3">
        <div className="flex items-center justify-between text-sm text-gray-600">
          <div className="flex items-center space-x-1">
            <Target className="h-4 w-4 text-green-600" />
            <span>Pass: {tournament.minPassingScore}%</span>
          </div>
          <div className="flex items-center space-x-1">
            <Clock className="h-4 w-4 text-orange-600" />
            <span>{daysLeft > 0 ? `${daysLeft} days left` : 'Last day'}</span>
          </div>
        </div>

        <div className="bg-gray-50 rounded p-3">
          <div className="flex items-center space-x-1 mb-1">
            <Calendar className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-medium text-gray-700">Duration</span>
          </div>
          <div className="text-sm text-gray-600">
            {new Date(tournament.startDate).toLocaleDateString()} - {new Date(tournament.endDate).toLocaleDateString()}
          </div>
        </div>

        {tournament.creator && (
          <div className="text-sm text-gray-500">
            Created by: {tournament.creator}
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="p-6 pt-0 flex space-x-3">
        <Button 
          onClick={() => onStart && onStart(tournament.id)}
          disabled={hasAttempted}
          className="flex-1"
        >
          {hasAttempted ? (
            <>
              <Trophy className="h-4 w-4 mr-2" />
              Completed
            </>
          ) : (
            <>
              <Play className="h-4 w-4 mr-2" />
              Start Quiz
            </>
          )}
        </Button>
        
        <Button 
          variant="outline" 
          onClick={() => onLike && onLike(tournament.id)}
          className="px-4"
        >
          <Star className="h-4 w-4" />
        </Button>
      </div>

      {/* Urgency warning for last few days */}
      {daysLeft <= 2 && daysLeft > 0 && (
        <div className="mx-6 mb-4 p-3 bg-orange-50 border-l-4 border-orange-400 rounded">
          <p className="text-sm text-orange-700 font-medium">
            Ending soon! Only {daysLeft} day{daysLeft !== 1 ? 's' : ''} remaining
          </p>
        </div>
      )}
    </Card>
  );
};

export default ImprovedTournamentCard;