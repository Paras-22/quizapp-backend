// src/components/dashboard/AdminDashboard.js
import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';
import { Trophy, Users, Clock, Star, Plus, TrendingUp } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import StatsCard from '../ui/StatsCard';
import TournamentForm from '../tournaments/TournamentForm';
import TournamentList from '../tournaments/TournamentList';

const AdminDashboard = () => {
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [stats, setStats] = useState({
    totalTournaments: 0,
    activeTournaments: 0,
    totalLikes: 0
  });

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const data = await apiService.getTournaments();
      setTournaments(data);
      
      // Calculate stats
      const now = new Date();
      const active = data.filter(t => new Date(t.endDate) > now);
      const totalLikes = data.reduce((sum, t) => sum + (t.likes || 0), 0);
      
      setStats({
        totalTournaments: data.length,
        activeTournaments: active.length,
        totalLikes: totalLikes
      });
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSuccess = () => {
    setShowCreateForm(false);
    loadDashboardData();
  };

  const handleDeleteTournament = async (id) => {
    if (window.confirm('Are you sure you want to delete this tournament? This action cannot be undone.')) {
      try {
        await apiService.deleteTournament(id);
        loadDashboardData();
      } catch (error) {
        console.error('Error deleting tournament:', error);
        alert('Failed to delete tournament. Please try again.');
      }
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
          <p className="text-gray-600">Manage tournaments and monitor activities</p>
        </div>
        <Button onClick={() => setShowCreateForm(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Create Tournament
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <StatsCard
          title="Total Tournaments"
          value={stats.totalTournaments}
          icon={<Trophy className="h-6 w-6 text-blue-600" />}
          color="blue"
        />
        <StatsCard
          title="Active Tournaments"
          value={stats.activeTournaments}
          icon={<Clock className="h-6 w-6 text-green-600" />}
          color="green"
        />
        <StatsCard
          title="Total Likes"
          value={stats.totalLikes}
          icon={<Star className="h-6 w-6 text-yellow-600" />}
          color="yellow"
        />
      </div>

      {/* Tournament Form Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
            <TournamentForm 
              onSuccess={handleCreateSuccess}
              onCancel={() => setShowCreateForm(false)}
            />
          </div>
        </div>
      )}

      {/* Tournaments List */}
      <Card>
        <h2 className="text-xl font-semibold mb-4">Tournaments</h2>
        <TournamentList 
          tournaments={tournaments}
          isAdmin={true}
          onDelete={handleDeleteTournament}
        />
      </Card>
    </div>
  );
};

export default AdminDashboard;