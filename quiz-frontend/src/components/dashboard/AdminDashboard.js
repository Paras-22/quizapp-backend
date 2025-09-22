// Enhanced AdminDashboard.js with tournament categorization
import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';
import { Trophy, Users, Clock, Star, Plus, TrendingUp, Table, Grid, Calendar, CheckCircle, PlayCircle } from 'lucide-react';
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
  const [activeCategory, setActiveCategory] = useState('all'); // 'all', 'upcoming', 'active', 'completed'
  const [categorizedTournaments, setCategorizedTournaments] = useState({
    upcoming: [],
    active: [],
    completed: [],
    all: []
  });
  const [stats, setStats] = useState({
    totalTournaments: 0,
    activeTournaments: 0,
    totalLikes: 0,
    upcomingTournaments: 0,
    completedTournaments: 0
  });

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const data = await apiService.getTournaments();
      setTournaments(data);
      
      // Categorize tournaments by status
      const categorized = categorizeTournaments(data);
      setCategorizedTournaments(categorized);
      
      // Calculate enhanced stats
      const now = new Date();
      const active = data.filter(t => new Date(t.startDate) <= now && new Date(t.endDate) >= now);
      const upcoming = data.filter(t => new Date(t.startDate) > now);
      const completed = data.filter(t => new Date(t.endDate) < now);
      const totalLikes = data.reduce((sum, t) => sum + (t.likes || 0), 0);
      
      setStats({
        totalTournaments: data.length,
        activeTournaments: active.length,
        upcomingTournaments: upcoming.length,
        completedTournaments: completed.length,
        totalLikes: totalLikes
      });
    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const categorizeTournaments = (tournaments) => {
    const now = new Date();
    
    const upcoming = tournaments.filter(tournament => {
      const startDate = new Date(tournament.startDate);
      return startDate > now;
    });
    
    const active = tournaments.filter(tournament => {
      const startDate = new Date(tournament.startDate);
      const endDate = new Date(tournament.endDate);
      return startDate <= now && endDate >= now;
    });
    
    const completed = tournaments.filter(tournament => {
      const endDate = new Date(tournament.endDate);
      return endDate < now;
    });

    return {
      upcoming: upcoming.sort((a, b) => new Date(a.startDate) - new Date(b.startDate)),
      active: active.sort((a, b) => new Date(b.startDate) - new Date(a.startDate)),
      completed: completed.sort((a, b) => new Date(b.endDate) - new Date(a.endDate)),
      all: tournaments
    };
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

  const getCategoryIcon = (category) => {
    switch (category) {
      case 'upcoming': return <Calendar className="h-4 w-4" />;
      case 'active': return <PlayCircle className="h-4 w-4" />;
      case 'completed': return <CheckCircle className="h-4 w-4" />;
      default: return <Trophy className="h-4 w-4" />;
    }
  };

  const getCategoryColor = (category) => {
    switch (category) {
      case 'upcoming': return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'active': return 'bg-green-100 text-green-700 border-green-200';
      case 'completed': return 'bg-gray-100 text-gray-700 border-gray-200';
      default: return 'bg-purple-100 text-purple-700 border-purple-200';
    }
  };

  const getCurrentTournaments = () => {
    return categorizedTournaments[activeCategory] || [];
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

      {/* Enhanced Stats Cards Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-8">
        <StatsCard
          title="Total Tournaments"
          value={stats.totalTournaments}
          icon={<Trophy className="h-6 w-6 text-purple-600" />}
          color="purple"
        />
        <StatsCard
          title="Upcoming"
          value={stats.upcomingTournaments}
          icon={<Calendar className="h-6 w-6 text-blue-600" />}
          color="blue"
        />
        <StatsCard
          title="Active"
          value={stats.activeTournaments}
          icon={<PlayCircle className="h-6 w-6 text-green-600" />}
          color="green"
        />
        <StatsCard
          title="Completed"
          value={stats.completedTournaments}
          icon={<CheckCircle className="h-6 w-6 text-gray-600" />}
          color="gray"
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

      {/* Main Content - Tournaments Section with Categories */}
      <Card>
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold">Tournaments</h2>
          <span className="text-sm text-gray-500">
            {getCurrentTournaments().length} tournament{getCurrentTournaments().length !== 1 ? 's' : ''}
          </span>
        </div>
        
        {/* Category Filter Tabs */}
        <div className="flex space-x-2 mb-6 overflow-x-auto">
          {[
            { key: 'all', label: 'All Tournaments', count: stats.totalTournaments },
            { key: 'upcoming', label: 'Upcoming', count: stats.upcomingTournaments },
            { key: 'active', label: 'Active', count: stats.activeTournaments },
            { key: 'completed', label: 'Completed', count: stats.completedTournaments }
          ].map(({ key, label, count }) => (
            <button
              key={key}
              onClick={() => setActiveCategory(key)}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg border-2 transition-all whitespace-nowrap ${
                activeCategory === key
                  ? getCategoryColor(key) + ' font-medium'
                  : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
              }`}
            >
              {getCategoryIcon(key)}
              <span>{label}</span>
              <span className={`text-xs px-2 py-1 rounded-full ${
                activeCategory === key ? 'bg-white bg-opacity-50' : 'bg-gray-100'
              }`}>
                {count}
              </span>
            </button>
          ))}
        </div>

        {/* Tournament Content */}
        {viewMode === 'table' ? (
          <TournamentTable
            tournaments={getCurrentTournaments()}
            onEdit={handleEditTournament}
            onDelete={handleDeleteTournament}
            loading={loading}
            showViewQuestions={false}
          />
        ) : (
          <TournamentList 
            tournaments={getCurrentTournaments()}
            isAdmin={true}
            onDelete={handleDeleteTournament}
            onEdit={handleEditTournament}
            showViewQuestions={false}
          />
        )}

        {/* Empty State */}
        {getCurrentTournaments().length === 0 && !loading && (
          <div className="text-center py-12">
            {getCategoryIcon(activeCategory)}
            <div className="mx-auto mb-4 h-12 w-12 text-gray-400">
              {getCategoryIcon(activeCategory)}
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              No {activeCategory === 'all' ? '' : activeCategory} tournaments yet
            </h3>
            <p className="text-gray-600 mb-4">
              {activeCategory === 'upcoming' && 'No tournaments are scheduled for the future'}
              {activeCategory === 'active' && 'No tournaments are currently running'}
              {activeCategory === 'completed' && 'No tournaments have been completed yet'}
              {activeCategory === 'all' && 'Get started by creating your first tournament'}
            </p>
            {(activeCategory === 'all' || activeCategory === 'upcoming') && (
              <Button onClick={() => setShowCreateForm(true)}>
                <Plus className="h-4 w-4 mr-2" />
                Create Tournament
              </Button>
            )}
          </div>
        )}
      </Card>

      {/* Enhanced Analytics Section */}
      {tournaments.length > 0 && (
        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-8">
          <Card>
            <h3 className="text-lg font-semibold mb-4">Tournament Distribution</h3>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <div className="flex items-center space-x-2">
                  <Calendar className="h-4 w-4 text-blue-600" />
                  <span>Upcoming</span>
                </div>
                <div className="flex items-center space-x-2">
                  <div className="w-24 bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-600 h-2 rounded-full"
                      style={{ 
                        width: `${stats.totalTournaments > 0 ? (stats.upcomingTournaments / stats.totalTournaments) * 100 : 0}%` 
                      }}
                    ></div>
                  </div>
                  <span className="text-sm font-medium">{stats.upcomingTournaments}</span>
                </div>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center space-x-2">
                  <PlayCircle className="h-4 w-4 text-green-600" />
                  <span>Active</span>
                </div>
                <div className="flex items-center space-x-2">
                  <div className="w-24 bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-green-600 h-2 rounded-full"
                      style={{ 
                        width: `${stats.totalTournaments > 0 ? (stats.activeTournaments / stats.totalTournaments) * 100 : 0}%` 
                      }}
                    ></div>
                  </div>
                  <span className="text-sm font-medium">{stats.activeTournaments}</span>
                </div>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center space-x-2">
                  <CheckCircle className="h-4 w-4 text-gray-600" />
                  <span>Completed</span>
                </div>
                <div className="flex items-center space-x-2">
                  <div className="w-24 bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-gray-600 h-2 rounded-full"
                      style={{ 
                        width: `${stats.totalTournaments > 0 ? (stats.completedTournaments / stats.totalTournaments) * 100 : 0}%` 
                      }}
                    ></div>
                  </div>
                  <span className="text-sm font-medium">{stats.completedTournaments}</span>
                </div>
              </div>
            </div>
          </Card>

          <Card>
            <h3 className="text-lg font-semibold mb-4">Quick Stats</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-yellow-600">
                  {Math.round(stats.totalLikes / Math.max(stats.totalTournaments, 1))}
                </div>
                <div className="text-sm text-gray-600">Avg Likes</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-purple-600">
                  {Math.round((stats.completedTournaments / Math.max(stats.totalTournaments, 1)) * 100)}%
                </div>
                <div className="text-sm text-gray-600">Completion Rate</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {stats.activeTournaments}
                </div>
                <div className="text-sm text-gray-600">Live Now</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-blue-600">
                  {stats.upcomingTournaments}
                </div>
                <div className="text-sm text-gray-600">Coming Soon</div>
              </div>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;