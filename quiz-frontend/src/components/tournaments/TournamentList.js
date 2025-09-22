// src/components/tournaments/TournamentList.js
import React from 'react';
import TournamentCard from './TournamentCard';

const TournamentList = ({ 
  tournaments, 
  isAdmin, 
  onStart, 
  onLike, 
  onDelete, 
    onEdit,       
  onViewQuestions,
  userAttempts 
}) => {
  if (!tournaments || tournaments.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <p>No tournaments available</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {tournaments.map(tournament => (
        <TournamentCard
          key={tournament.id}
          tournament={tournament}
          onStart={onStart}
          onLike={onLike}
          onDelete={onDelete}
              onEdit={onEdit}              
    onViewQuestions={onViewQuestions} 
          isAdmin={isAdmin}
          userAttempts={userAttempts}
        />
      ))}
    </div>
  );
};

export default TournamentList;