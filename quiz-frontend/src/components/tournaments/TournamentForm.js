// Enhanced TournamentForm.js 
import React, { useState } from 'react';
import { apiService } from '../../services/api';
import { X, Trophy, Calendar, User, Settings, Info } from 'lucide-react';
import Button from '../ui/Button';
import Input from '../ui/Input';
import { DIFFICULTY_LEVELS, CATEGORIES } from '../../utils/constants';

const TournamentForm = ({ tournament = null, onSuccess, onCancel }) => {
  const [formData, setFormData] = useState({
    creator: tournament?.creator || '',
    name: tournament?.name || '',
    category: tournament?.category || CATEGORIES[0],
    difficulty: tournament?.difficulty || DIFFICULTY_LEVELS[1],
    startDate: tournament?.startDate || new Date().toISOString().split('T')[0],
    endDate: tournament?.endDate || new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    minPassingScore: tournament?.minPassingScore || 70
  });
  
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const isEditing = !!tournament;

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.creator.trim()) {
      newErrors.creator = 'Creator name is required';
    } else if (formData.creator.trim().length < 2) {
      newErrors.creator = 'Creator name must be at least 2 characters';
    }
    
    if (!formData.name.trim()) {
      newErrors.name = 'Tournament name is required';
    } else if (formData.name.trim().length < 3) {
      newErrors.name = 'Tournament name must be at least 3 characters';
    }
    
    const startDate = new Date(formData.startDate);
    const endDate = new Date(formData.endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (startDate < today && !isEditing) {
      newErrors.startDate = 'Start date cannot be in the past';
    }
    
    if (endDate <= startDate) {
      newErrors.endDate = 'End date must be after start date';
    }
    
    if (formData.minPassingScore < 0 || formData.minPassingScore > 100) {
      newErrors.minPassingScore = 'Passing score must be between 0 and 100';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);

    try {
      if (isEditing) {
        await apiService.updateTournament(tournament.id, formData);
      } else {
        await apiService.createTournament(formData);
      }
      onSuccess();
    } catch (error) {
      setErrors({ submit: 'Failed to save tournament. Please try again.' });
      console.error('Error saving tournament:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg w-full max-w-6xl max-h-screen overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b p-8 flex justify-between items-center rounded-t-lg">
          <div className="flex items-center space-x-4">
            <div className="p-3 bg-blue-100 rounded-full">
              <Trophy className="h-8 w-8 text-blue-600" />
            </div>
            <div>
              <h2 className="text-3xl font-bold text-gray-900">
                {isEditing ? 'Update Tournament' : 'Create New Tournament'}
              </h2>
              <p className="text-lg text-gray-600 mt-1">
                {isEditing ? 'Modify tournament details' : 'Set up a new quiz tournament for players'}
              </p>
            </div>
          </div>
          <Button variant="outline" size="lg" onClick={onCancel}>
            <X className="h-6 w-6" />
          </Button>
        </div>

        <div className="p-8">
          {/* Tournament Information Section */}
          <div className="mb-10">
            <div className="flex items-center space-x-3 mb-6">
              <Info className="h-6 w-6 text-blue-600" />
              <h3 className="text-2xl font-semibold text-gray-900">Tournament Information</h3>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="space-y-6">
                <div>
                  <label className="block text-lg font-medium text-gray-700 mb-3">
                    Creator Name *
                  </label>
                  <input
                    name="creator"
                    value={formData.creator}
                    onChange={handleChange}
                    placeholder="Enter creator name"
                    className={`w-full px-4 py-3 text-lg border-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                      errors.creator ? 'border-red-500' : 'border-gray-300'
                    }`}
                    required
                  />
                  {errors.creator && <p className="mt-2 text-sm text-red-600">{errors.creator}</p>}
                </div>

                <div>
                  <label className="block text-lg font-medium text-gray-700 mb-3">
                    Tournament Name *
                  </label>
                  <input
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Enter tournament name"
                    className={`w-full px-4 py-3 text-lg border-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                      errors.name ? 'border-red-500' : 'border-gray-300'
                    }`}
                    required
                  />
                  {errors.name && <p className="mt-2 text-sm text-red-600">{errors.name}</p>}
                </div>
              </div>

              <div className="space-y-6">
                <div>
                  <label className="block text-lg font-medium text-gray-700 mb-3">
                    Category *
                  </label>
                  <select
                    name="category"
                    value={formData.category}
                    onChange={handleChange}
                    className="w-full px-4 py-3 text-lg border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                    required
                  >
                    {CATEGORIES.map(category => (
                      <option key={category} value={category}>{category}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-lg font-medium text-gray-700 mb-3">
                    Difficulty Level *
                  </label>
                  <select
                    name="difficulty"
                    value={formData.difficulty}
                    onChange={handleChange}
                    className="w-full px-4 py-3 text-lg border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                    required
                  >
                    {DIFFICULTY_LEVELS.map(level => (
                      <option key={level} value={level}>{level}</option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          </div>

          {/* Schedule Section */}
          <div className="mb-10">
            <div className="flex items-center space-x-3 mb-6">
              <Calendar className="h-6 w-6 text-green-600" />
              <h3 className="text-2xl font-semibold text-gray-900">Tournament Schedule</h3>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div>
                <label className="block text-lg font-medium text-gray-700 mb-3">
                  Start Date *
                </label>
                <input
                  name="startDate"
                  type="date"
                  value={formData.startDate}
                  onChange={handleChange}
                  className={`w-full px-4 py-3 text-lg border-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                    errors.startDate ? 'border-red-500' : 'border-gray-300'
                  }`}
                  required
                />
                {errors.startDate && <p className="mt-2 text-sm text-red-600">{errors.startDate}</p>}
                <p className="mt-2 text-sm text-gray-600">When players can start participating</p>
              </div>

              <div>
                <label className="block text-lg font-medium text-gray-700 mb-3">
                  End Date *
                </label>
                <input
                  name="endDate"
                  type="date"
                  value={formData.endDate}
                  onChange={handleChange}
                  className={`w-full px-4 py-3 text-lg border-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                    errors.endDate ? 'border-red-500' : 'border-gray-300'
                  }`}
                  required
                />
                {errors.endDate && <p className="mt-2 text-sm text-red-600">{errors.endDate}</p>}
                <p className="mt-2 text-sm text-gray-600">Last day for participation</p>
              </div>
            </div>
          </div>

          {/* Settings Section */}
          <div className="mb-10">
            <div className="flex items-center space-x-3 mb-6">
              <Settings className="h-6 w-6 text-purple-600" />
              <h3 className="text-2xl font-semibold text-gray-900">Tournament Settings</h3>
            </div>
            
            <div className="max-w-md">
              <label className="block text-lg font-medium text-gray-700 mb-3">
                Minimum Passing Score (%) *
              </label>
              <input
                name="minPassingScore"
                type="number"
                min="0"
                max="100"
                value={formData.minPassingScore}
                onChange={handleChange}
                className={`w-full px-4 py-3 text-lg border-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                  errors.minPassingScore ? 'border-red-500' : 'border-gray-300'
                }`}
                required
              />
              {errors.minPassingScore && <p className="mt-2 text-sm text-red-600">{errors.minPassingScore}</p>}
              <p className="mt-2 text-sm text-gray-600">Percentage score needed to pass this tournament</p>
            </div>
          </div>

          {/* Information Box */}
          <div className="bg-blue-50 border-l-4 border-blue-400 p-6 mb-8">
            <div className="flex">
              <div className="flex-shrink-0">
                <Info className="h-6 w-6 text-blue-400" />
              </div>
              <div className="ml-3">
                <h4 className="text-lg font-medium text-blue-800">Tournament Creation Notes</h4>
                <div className="mt-2 text-blue-700">
                  <ul className="list-disc pl-5 space-y-1">
                    <li>10 questions will be automatically generated from an external quiz API</li>
                    <li>Questions will be multiple choice with 4 options each</li>
                    <li>Players can only attempt each tournament once</li>
                    <li>Tournament dates can be modified after creation if needed</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {/* Error Display */}
          {errors.submit && (
            <div className="p-4 bg-red-100 border-l-4 border-red-400 text-red-700 rounded-lg mb-6">
              <div className="flex">
                <div className="ml-3">
                  <p className="text-lg font-medium">Error</p>
                  <p className="mt-1">{errors.submit}</p>
                </div>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-end space-x-4 pt-8 border-t">
            <Button type="button" variant="secondary" size="lg" onClick={onCancel}>
              Cancel
            </Button>
            <Button 
              onClick={handleSubmit} 
              disabled={loading} 
              size="lg" 
              className="px-8"
            >
              {loading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  {isEditing ? 'Updating...' : 'Creating...'}
                </div>
              ) : (
                <>
                  <Trophy className="h-5 w-5 mr-2" />
                  {isEditing ? 'Update Tournament' : 'Create Tournament'}
                </>
              )}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TournamentForm;