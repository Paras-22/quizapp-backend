// src/components/dashboard/PlayerDashboard.js
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../../services/api';
import { Trophy, Play, Award, TrendingUp, Clock, Star } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import StatsCard from '../ui/StatsCard';
import TournamentList from '../tournaments/TournamentList';

const PlayerDashboard = () => {
  const [tournaments, setTournaments] = useState([]);
  const [attempts, setAttempts] = useState([]);
  const [stats, setStats] = useState({
    tournamentsPlayed: 0,
    averageScore: 0,
    bestScore: 0,
    totalPoints: 0
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadDashboardData();
  }, []);

  

const loadDashboardData = async () => {
  try {
    const [tournamentsData, attemptsData] = await Promise.all([
      apiService.getTournaments(),
      apiService.getMyAttempts()
    ]);
    
    setTournaments(tournamentsData);
    setAttempts(attemptsData);
    
    // Calculate stats from attempts data as fallback
    const completedAttempts = attemptsData.filter(attempt => attempt.completed);
    const fallbackStats = {
      tournamentsPlayed: completedAttempts.length,
      averageScore: completedAttempts.length > 0 
        ? completedAttempts.reduce((sum, attempt) => sum + attempt.score, 0) / completedAttempts.length 
        : 0,
      bestScore: completedAttempts.length > 0 
        ? Math.max(...completedAttempts.map(attempt => attempt.score)) 
        : 0,
      totalPoints: completedAttempts.reduce((sum, attempt) => sum + attempt.score, 0)
    };
    
    // Try to get stats from backend, use fallback if fails
    try {
      const statsData = await apiService.getPlayerStats();
      setStats(statsData);
    } catch (statsError) {
      console.warn('Failed to load stats from backend, using calculated stats:', statsError);
      setStats(fallbackStats);
    }
    
  } catch (error) {
    console.error('Error loading dashboard data:', error);
    // Set default values on complete failure
    setStats({
      tournamentsPlayed: 0,
      averageScore: 0,
      bestScore: 0,
      totalPoints: 0
    });
  } finally {
    setLoading(false);
  }
};

  const handleStartTournament = async (tournamentId) => {
    try {
      const result = await apiService.startTournament(tournamentId);
      navigate(`/quiz/${tournamentId}`, { state: { attemptId: result.id } });
    } catch (error) {
      console.error('Error starting tournament:', error);
      alert('Failed to start tournament. Please try again.');
    }
  };

  const handleLikeTournament = async (tournamentId) => {
    try {
      await apiService.likeTournament(tournamentId);
      loadDashboardData(); // Refresh to show updated likes
    } catch (error) {
      console.error('Error liking tournament:', error);
    }
  };

  const availableTournaments = tournaments.filter(tournament => {
    const now = new Date();
    const startDate = new Date(tournament.startDate);
    const endDate = new Date(tournament.endDate);
    const hasAttempted = attempts.some(attempt => attempt.tournament.id === tournament.id);
    
    return startDate <= now && endDate >= now && !hasAttempted;
  });

  const completedTournaments = tournaments.filter(tournament => {
    return attempts.some(attempt => 
      attempt.tournament.id === tournament.id && attempt.completed
    );
  });

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Player Dashboard</h1>
        <p className="text-gray-600">Track your progress and join tournaments</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatsCard
          title="Tournaments Played"
          value={stats.tournamentsPlayed}
          icon={<Trophy className="h-6 w-6 text-blue-600" />}
          color="blue"
        />
        <StatsCard
          title="Average Score"
          value={`${stats.averageScore.toFixed(1)}/10`}
          icon={<TrendingUp className="h-6 w-6 text-green-600" />}
          color="green"
        />
        <StatsCard
          title="Best Score"
          value={`${stats.bestScore}/10`}
          icon={<Award className="h-6 w-6 text-yellow-600" />}
          color="yellow"
        />
        <StatsCard
          title="Total Points"
          value={stats.totalPoints}
          icon={<Star className="h-6 w-6 text-purple-600" />}
          color="purple"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Available Tournaments */}
        <Card>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">Available Tournaments</h2>
            <Play className="h-5 w-5 text-green-600" />
          </div>
          {availableTournaments.length > 0 ? (
            <TournamentList 
              tournaments={availableTournaments}
              isAdmin={false}
              onStart={handleStartTournament}
              onLike={handleLikeTournament}
              userAttempts={attempts}
            />
          ) : (
            <div className="text-center py-8">
              <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500">No tournaments available right now</p>
              <p className="text-sm text-gray-400">Check back later for new tournaments</p>
            </div>
          )}
        </Card>

        {/* Completed Tournaments */}
        <Card>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">Completed Tournaments</h2>
            <Award className="h-5 w-5 text-blue-600" />
          </div>
          {completedTournaments.length > 0 ? (
            <div className="space-y-4">
              {completedTournaments.map(tournament => {
                const attempt = attempts.find(a => 
                  a.tournament.id === tournament.id && a.completed
                );
                return (
                  <div key={tournament.id} className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                    <div>
                      <h3 className="font-medium">{tournament.name}</h3>
                      <p className="text-sm text-gray-600">{tournament.category}</p>
                    </div>
                    <div className="text-right">
                      <div className="font-semibold text-lg">
                        {attempt?.score || 0}/10
                      </div>
                      <div className="text-sm text-gray-500">
                        {attempt?.score >= tournament.minPassingScore ? (
                          <span className="text-green-600">Passed</span>
                        ) : (
                          <span className="text-red-600">Failed</span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-8">
              <Trophy className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500">No completed tournaments yet</p>
              <p className="text-sm text-gray-400">Start a tournament to see your results here</p>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default PlayerDashboard;