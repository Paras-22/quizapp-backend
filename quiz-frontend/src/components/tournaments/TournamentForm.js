// Enhanced TournamentForm.js with creator field and better validation
import React, { useState } from 'react';
import { apiService } from '../../services/api';
import { X } from 'lucide-react';
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
    
    // Creator validation
    if (!formData.creator.trim()) {
      newErrors.creator = 'Creator name is required';
    } else if (formData.creator.trim().length < 2) {
      newErrors.creator = 'Creator name must be at least 2 characters';
    }
    
    // Tournament name validation
    if (!formData.name.trim()) {
      newErrors.name = 'Tournament name is required';
    } else if (formData.name.trim().length < 3) {
      newErrors.name = 'Tournament name must be at least 3 characters';
    }
    
    // Date validation
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
    
    // Passing score validation
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
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-semibold">
          {isEditing ? 'Update Tournament' : 'Create New Tournament'}
        </h2>
        <Button variant="outline" size="sm" onClick={onCancel}>
          <X className="h-4 w-4" />
        </Button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Creator *"
          name="creator"
          value={formData.creator}
          onChange={handleChange}
          placeholder="Enter creator name"
          error={errors.creator}
          required
        />

        <Input
          label="Tournament Name *"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Enter tournament name"
          error={errors.name}
          required
        />

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Category *
            </label>
            <select
              name="category"
              value={formData.category}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            >
              {CATEGORIES.map(category => (
                <option key={category} value={category}>{category}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Difficulty *
            </label>
            <select
              name="difficulty"
              value={formData.difficulty}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            >
              {DIFFICULTY_LEVELS.map(level => (
                <option key={level} value={level}>{level}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Start Date *"
            name="startDate"
            type="date"
            value={formData.startDate}
            onChange={handleChange}
            error={errors.startDate}
            required
          />

          <Input
            label="End Date *"
            name="endDate"
            type="date"
            value={formData.endDate}
            onChange={handleChange}
            error={errors.endDate}
            required
          />
        </div>

        <Input
          label="Minimum Passing Score (%) *"
          name="minPassingScore"
          type="number"
          min="0"
          max="100"
          value={formData.minPassingScore}
          onChange={handleChange}
          error={errors.minPassingScore}
          required
        />

        {errors.submit && (
          <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
            {errors.submit}
          </div>
        )}

        <div className="flex space-x-3 pt-4">
          <Button type="submit" disabled={loading} className="flex-1">
            {loading ? 
              (isEditing ? 'Updating...' : 'Creating...') : 
              (isEditing ? 'Update Tournament' : 'Create Tournament')
            }
          </Button>
          <Button type="button" variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
};

export default TournamentForm;