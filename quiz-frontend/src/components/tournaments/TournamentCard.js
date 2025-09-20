// src/components/tournaments/TournamentCard.js
import React from 'react';
import { Calendar, Star, Play, Trophy, Clock } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';

const TournamentCard = ({ 
  tournament, 
  onStart, 
  onLike, 
  onDelete, 
  isAdmin, 
  userAttempts = [] 
}) => {
  const hasAttempted = userAttempts.some(
    attempt => attempt.tournament.id === tournament.id
  );
  
  const isOngoing = () => {
    const now = new Date();
    const startDate = new Date(tournament.startDate);
    const endDate = new Date(tournament.endDate);
    return startDate <= now && endDate >= now;
  };

  const getStatusBadge = () => {
    const now = new Date();
    const startDate = new Date(tournament.startDate);
    const endDate = new Date(tournament.endDate);
    
    if (startDate > now) {
      return <span className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs rounded-full">Upcoming</span>;
    } else if (endDate < now) {
      return <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs rounded-full">Completed</span>;
    } else {
      return <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full">Active</span>;
    }
  };

  return (
    <Card className="h-full transform transition-all duration-200 hover:shadow-lg hover:-translate-y-1">
      <div className="flex justify-between items-start mb-4">
        <div className="flex-1">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-lg font-semibold text-gray-900 overflow-hidden">
              {tournament.name}
            </h3>
            {getStatusBadge()}
          </div>
          <div className="space-y-1 text-sm text-gray-600">
            <p className="flex items-center">
              <Trophy className="h-4 w-4 mr-1" />
              {tournament.category} • {tournament.difficulty}
            </p>
            <p className="flex items-center">
              <Calendar className="h-4 w-4 mr-1" />
              {new Date(tournament.startDate).toLocaleDateString()} - {new Date(tournament.endDate).toLocaleDateString()}
            </p>
            <p className="flex items-center">
              <Clock className="h-4 w-4 mr-1" />
              Passing Score: {tournament.minPassingScore}%
            </p>
          </div>
        </div>
        
        <div className="flex items-center space-x-1 ml-4">
          <Star className="h-4 w-4 text-yellow-500" />
          <span className="text-sm text-gray-600">{tournament.likes || 0}</span>
        </div>
      </div>
      
      <div className="flex space-x-2 mt-4">
        {!isAdmin && (
          <>
            <Button 
              size="sm" 
              onClick={() => onStart && onStart(tournament.id)}
              disabled={hasAttempted || !isOngoing()}
              variant={hasAttempted ? 'secondary' : 'primary'}
              className="flex-1"
            >
              <Play className="h-4 w-4 mr-1" />
              {hasAttempted ? 'Completed' : isOngoing() ? 'Start Quiz' : 'Not Available'}
            </Button>
            <Button 
              size="sm" 
              variant="outline" 
              onClick={() => onLike && onLike(tournament.id)}
              className="px-3"
            >
              <Star className="h-4 w-4" />
            </Button>
          </>
        )}
        
        {isAdmin && (
          <Button 
            size="sm" 
            variant="danger" 
            onClick={() => onDelete && onDelete(tournament.id)}
            className="flex-1"
          >
            Delete
          </Button>
        )}
      </div>
    </Card>
  );
};

export default TournamentCard;