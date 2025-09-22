// Simplified AdminDashboard.js 
import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';
import { Trophy, Users, Clock, Star, Plus, TrendingUp, Table, Grid } from 'lucide-react';
import Button from '../ui/Button';
import Card from '../ui/Card';
import StatsCard from '../ui/StatsCard';
import TournamentForm from '../tournaments/TournamentForm';
import TournamentTable from '../tournaments/TournamentTable';
import TournamentList from '../tournaments/TournamentList';

const AdminDashboard = () => {
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [editingTournament, setEditingTournament] = useState(null);
  const [viewMode, setViewMode] = useState('table'); // 'table' or 'cards'
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

  const handleEditSuccess = () => {
    setShowEditForm(false);
    setEditingTournament(null);
    loadDashboardData();
  };

  const handleEditTournament = (tournament) => {
    setEditingTournament(tournament);
    setShowEditForm(true);
  };

  const handleDeleteTournament = async (id) => {
    const tournament = tournaments.find(t => t.id === id);
    const confirmMessage = `Are you sure you want to delete "${tournament?.name}"?\n\nThis action cannot be undone and will delete all related attempts and answers.`;
    
    if (window.confirm(confirmMessage)) {
      try {
        setLoading(true);
        const result = await apiService.deleteTournament(id);
        
        if (result && result.success) {
          alert(result.message || 'Tournament deleted successfully!');
        } else {
          alert('Tournament deleted successfully!');
        }
        
        await loadDashboardData();
      } catch (error) {
        console.error('Error deleting tournament:', error);
        alert(`Failed to delete tournament: ${error.message || 'Please try again.'}`);
      } finally {
        setLoading(false);
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
      {/* Header Section */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
          <p className="text-gray-600">Manage tournaments and monitor activities</p>
        </div>
        <div className="flex space-x-3">
          {/* View Mode Toggle */}
          <div className="flex bg-gray-200 rounded-lg p-1">
            <Button
              size="sm"
              variant={viewMode === 'table' ? 'primary' : 'secondary'}
              onClick={() => setViewMode('table')}
              className="px-3"
            >
              <Table className="h-4 w-4 mr-1" />
              Table
            </Button>
            <Button
              size="sm"
              variant={viewMode === 'cards' ? 'primary' : 'secondary'}
              onClick={() => setViewMode('cards')}
              className="px-3"
            >
              <Grid className="h-4 w-4 mr-1" />
              Cards
            </Button>
          </div>
          {/* Create Tournament Button */}
          <Button onClick={() => setShowCreateForm(true)}>
            <Plus className="h-4 w-4 mr-2" />
            Create Tournament
          </Button>
        </div>
      </div>

      {/* Stats Cards Section */}
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

      {/* Create Tournament Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
            <TournamentForm 
              onSuccess={handleCreateSuccess}
              onCancel={() => setShowCreateForm(false)}
            />
          </div>
        </div>
      )}

      {/* Edit Tournament Modal */}
      {showEditForm && editingTournament && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
            <TournamentForm 
              tournament={editingTournament}
              onSuccess={handleEditSuccess}
              onCancel={() => {
                setShowEditForm(false);
                setEditingTournament(null);
              }}
            />
          </div>
        </div>
      )}

      {/* Main Content - Tournaments Section */}
      <Card>
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">Tournaments</h2>
          <span className="text-sm text-gray-500">
            {tournaments.length} tournament{tournaments.length !== 1 ? 's' : ''}
          </span>
        </div>
        
        {/* Conditional Rendering based on view mode */}
        {viewMode === 'table' ? (
          <TournamentTable
            tournaments={tournaments}
            onEdit={handleEditTournament}
            onDelete={handleDeleteTournament}
            loading={loading}
            showViewQuestions={false}
          />
        ) : (
          <TournamentList 
            tournaments={tournaments}
            isAdmin={true}
            onDelete={handleDeleteTournament}
            onEdit={handleEditTournament}
            showViewQuestions={false}
          />
        )}

        {/* Empty State */}
        {tournaments.length === 0 && !loading && (
          <div className="text-center py-12">
            <Trophy className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No tournaments yet</h3>
            <p className="text-gray-600 mb-4">Get started by creating your first tournament</p>
            <Button onClick={() => setShowCreateForm(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Create Your First Tournament
            </Button>
          </div>
        )}
      </Card>

      {/* Additional Analytics Section */}
      {tournaments.length > 0 && (
        <div className="mt-8">
          <Card>
            <h3 className="text-lg font-semibold mb-4">Quick Analytics</h3>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-600">
                  {tournaments.filter(t => new Date(t.startDate) > new Date()).length}
                </div>
                <div className="text-sm text-gray-600">Upcoming</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {tournaments.filter(t => {
                    const now = new Date();
                    return new Date(t.startDate) <= now && new Date(t.endDate) >= now;
                  }).length}
                </div>
                <div className="text-sm text-gray-600">Active</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-600">
                  {tournaments.filter(t => new Date(t.endDate) < new Date()).length}
                </div>
                <div className="text-sm text-gray-600">Completed</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-yellow-600">
                  {Math.round(stats.totalLikes / tournaments.length) || 0}
                </div>
                <div className="text-sm text-gray-600">Avg Likes</div>
              </div>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;