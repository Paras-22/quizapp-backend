// Updated PlayerDashboard.js with participant count and global ranking
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../../services/api';
import { Trophy, Award, TrendingUp, Star, Clock, Play, Users, Medal } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import StatsCard from '../ui/StatsCard';
import ImprovedTournamentCard from './ImprovedTournamentCard';
import QuizReviewModal from '../tournaments/QuizReviewModal';

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
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedAttempt, setSelectedAttempt] = useState(null);

  // fetch participant count for each tournament
  const [participantCounts, setParticipantCounts] = useState({});

  // fetch global ranking for current player
  const [globalRanking, setGlobalRanking] = useState(null);

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

      try {
        const statsData = await apiService.getPlayerStats();
        setStats(statsData);
      } catch (statsError) {
        console.warn('Failed to load stats from backend, using calculated stats:', statsError);
        setStats(fallbackStats);
      }

      // fetch participant count for each completed tournament using scoreboard endpoint
      const completedTournamentIds = attemptsData
        .filter(a => a.completed)
        .map(a => a.tournament.id);

      const counts = {};
      await Promise.all(
        completedTournamentIds.map(async (id) => {
          try {
            const scoreboard = await apiService.getScoreboard(id);
            counts[id] = scoreboard.totalPlayers || 0;
          } catch (err) {
            counts[id] = 0;
          }
        })
      );
      setParticipantCounts(counts);

      // fetch global ranking for current player
      try {
        const rankingData = await apiService.getLeaderboardPosition();
        setGlobalRanking(rankingData);
      } catch (rankErr) {
        console.warn('Could not load global ranking:', rankErr);
        setGlobalRanking(null);
      }

    } catch (error) {
      console.error('Error loading dashboard data:', error);
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

  const handleReviewQuiz = (tournament) => {
    const attempt = attempts.find(a =>
      a.tournament.id === tournament.id && a.completed
    );
    setSelectedAttempt(attempt);
    setShowReviewModal(true);
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
      loadDashboardData();
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

      {/* Stats Cards — unchanged */}
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
        {/* Available Tournaments — unchanged */}
        <Card>
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-xl font-semibold">Available Tournaments</h2>
              <p className="text-sm text-gray-600">{availableTournaments.length} tournaments ready to join</p>
            </div>
            <Play className="h-5 w-5 text-green-600" />
          </div>

          {availableTournaments.length > 0 ? (
            <div className="space-y-6">
              {availableTournaments.map(tournament => (
                <ImprovedTournamentCard
                  key={tournament.id}
                  tournament={tournament}
                  onStart={handleStartTournament}
                  onLike={handleLikeTournament}
                  userAttempts={attempts}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500">No tournaments available right now</p>
              <p className="text-sm text-gray-400">Check back later for new tournaments</p>
            </div>
          )}
        </Card>

        {/* Completed Tournaments — now shows participant count badge */}
        <Card>
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-xl font-semibold">Completed Tournaments</h2>
              <p className="text-sm text-gray-600">{completedTournaments.length} tournaments completed</p>
            </div>
            <Award className="h-5 w-5 text-blue-600" />
          </div>

          {completedTournaments.length > 0 ? (
            <div className="space-y-4">
              {completedTournaments.map(tournament => {
                const attempt = attempts.find(a =>
                  a.tournament.id === tournament.id && a.completed
                );
                const totalParticipants = participantCounts[tournament.id] || 0;

                return (
                  <div key={tournament.id} className="p-4 bg-gray-50 rounded-lg">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <h3 className="font-medium">{tournament.name}</h3>
                        <p className="text-sm text-gray-600">{tournament.category}</p>
                        <p className="text-xs text-gray-500">
                          Completed: {attempt?.completedAt
                            ? new Date(attempt.completedAt).toLocaleDateString()
                            : 'N/A'}
                        </p>
                        {/* participant count badge */}
                        <div className="flex items-center gap-1 mt-1">
                          <Users className="h-3 w-3 text-blue-500" />
                          <span className="text-xs text-blue-600 font-medium">
                            {totalParticipants} player{totalParticipants !== 1 ? 's' : ''} participated
                          </span>
                        </div>
                      </div>
                      <div className="text-right flex items-center space-x-3">
                        <div>
                          <div className="font-semibold text-lg">
                            {attempt?.score || 0}/10
                          </div>
                          <div className="text-sm">
                            {attempt?.score >= tournament.minPassingScore ? (
                              <span className="text-green-600">Passed</span>
                            ) : (
                              <span className="text-red-600">Failed</span>
                            )}
                          </div>
                        </div>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleReviewQuiz(tournament)}
                          className="px-3"
                        >
                          Review
                        </Button>
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

      {/* global ranking section showing player position */}
      <div className="mt-8">
        <Card>
          <div className="flex items-center gap-3 mb-4">
            <Medal className="h-6 w-6 text-yellow-500" />
            <h2 className="text-xl font-semibold text-gray-900">Your Global Ranking</h2>
          </div>

          {globalRanking ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Rank card */}
              <div className="bg-gradient-to-br from-yellow-50 to-amber-50 border border-yellow-200 rounded-xl p-5 text-center">
                <Trophy className="h-8 w-8 text-yellow-500 mx-auto mb-2" />
                <p className="text-sm text-gray-500 mb-1">Global Rank</p>
                <p className="text-3xl font-bold text-yellow-600">
                  {globalRanking.globalRank === 'Not implemented yet'
                    ? '—'
                    : `#${globalRanking.globalRank}`}
                </p>
                <p className="text-xs text-gray-400 mt-1">
                  {globalRanking.globalRank === 'Not implemented yet'
                    ? 'Coming soon'
                    : 'among all players'}
                </p>
              </div>

              {/* Total players card */}
              <div className="bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-5 text-center">
                <Users className="h-8 w-8 text-blue-500 mx-auto mb-2" />
                <p className="text-sm text-gray-500 mb-1">Total Players</p>
                <p className="text-3xl font-bold text-blue-600">
                  {globalRanking.totalPlayers || 0}
                </p>
                <p className="text-xs text-gray-400 mt-1">registered on the platform</p>
              </div>

              {/* Tournaments attempted card */}
              <div className="bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200 rounded-xl p-5 text-center">
                <Award className="h-8 w-8 text-green-500 mx-auto mb-2" />
                <p className="text-sm text-gray-500 mb-1">Your Attempts</p>
                <p className="text-3xl font-bold text-green-600">
                  {globalRanking.playerAttempts || 0}
                </p>
                <p className="text-xs text-gray-400 mt-1">total quiz attempts</p>
              </div>
            </div>
          ) : (
            <div className="text-center py-8">
              <Medal className="h-12 w-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500">Complete a tournament to see your global ranking</p>
            </div>
          )}
        </Card>
      </div>

      <QuizReviewModal
        attempt={selectedAttempt}
        isOpen={showReviewModal}
        onClose={() => {
          setShowReviewModal(false);
          setSelectedAttempt(null);
        }}
      />
    </div>
  );
};

export default PlayerDashboard;