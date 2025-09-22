// Fixed ProfileModal.js - Profile management modal for both admin and player
import React, { useState, useEffect } from 'react';
import { X, User, Mail, Phone, MapPin, Calendar, Edit, Save, Camera } from 'lucide-react';
import Button from '../ui/Button';
import Input from '../ui/Input';
import { apiService } from '../../services/api';

const ProfileModal = ({ isOpen, onClose, onProfileUpdate }) => {
  const [profileData, setProfileData] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    bio: '',
    dateOfBirth: '',
    profilePicture: ''
  });
  
  const [originalData, setOriginalData] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});
  const [isEditing, setIsEditing] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    if (isOpen) {
      loadProfile();
    }
  }, [isOpen]);

  const loadProfile = async () => {
    setLoading(true);
    setErrors({});
    try {
      const data = await apiService.getProfile();
      // Ensure all fields have values (empty string if null/undefined)
      const normalizedData = {
        username: data.username || '',
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        email: data.email || '',
        phone: data.phone || '',
        address: data.address || '',
        bio: data.bio || '',
        dateOfBirth: data.dateOfBirth || '',
        profilePicture: data.profilePicture || ''
      };
      setProfileData(normalizedData);
      setOriginalData(normalizedData);
    } catch (error) {
      setErrors({ load: 'Failed to load profile data' });
      console.error('Error loading profile:', error);
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    // Username validation
    if (!profileData.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (profileData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }
    
    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!profileData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!emailRegex.test(profileData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }
    
    // Name validation
    if (!profileData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }
    if (!profileData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    
    // Phone validation (optional but must be valid if provided)
    if (profileData.phone.trim() && !/^[\+]?[1-9][\d]{0,15}$/.test(profileData.phone.replace(/\s/g, ''))) {
      newErrors.phone = 'Please enter a valid phone number';
    }
    
    // Date of birth validation (optional but must be valid if provided)
    if (profileData.dateOfBirth) {
      const birthDate = new Date(profileData.dateOfBirth);
      const today = new Date();
      const age = today.getFullYear() - birthDate.getFullYear();
      if (age < 13 || age > 120) {
        newErrors.dateOfBirth = 'Please enter a valid date of birth';
      }
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) {
      return;
    }
    
    setSaving(true);
    setErrors({});
    setSuccessMessage('');
    
    try {
      // Send only the fields that are different from original
      const updatedProfile = await apiService.updateProfile(profileData);
      
      // Update both current and original data
      setOriginalData({ ...profileData });
      setIsEditing(false);
      setSuccessMessage('Profile updated successfully!');
      
      // Notify parent component of profile update
      if (onProfileUpdate) {
        onProfileUpdate(updatedProfile);
      }
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
      
    } catch (error) {
      setErrors({ save: 'Failed to update profile. Please try again.' });
      console.error('Error updating profile:', error);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setProfileData({ ...originalData });
    setIsEditing(false);
    setErrors({});
    setSuccessMessage('');
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfileData(prev => ({
      ...prev,
      [name]: value || '' // Ensure no null values
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  // Fixed hasChanges function
  const hasChanges = () => {
    return Object.keys(profileData).some(key => 
      (profileData[key] || '') !== (originalData[key] || '')
    );
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-96 overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 bg-white border-b p-6 flex justify-between items-center">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-blue-100 rounded-full">
              <User className="h-6 w-6 text-blue-600" />
            </div>
            <div>
              <h2 className="text-xl font-semibold">Profile Settings</h2>
              <p className="text-gray-600 text-sm">Manage your account information</p>
            </div>
          </div>
          <Button variant="outline" size="sm" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Content */}
        <div className="p-6">
          {loading && (
            <div className="flex justify-center items-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          )}

          {errors.load && (
            <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg mb-4">
              {errors.load}
            </div>
          )}

          {successMessage && (
            <div className="p-4 bg-green-100 border border-green-400 text-green-700 rounded-lg mb-4">
              {successMessage}
            </div>
          )}

          {!loading && (
            <div className="space-y-6">
              {/* Profile Picture Section */}
              <div className="flex items-center space-x-4">
                <div className="relative">
                  <div className="w-20 h-20 bg-gray-200 rounded-full flex items-center justify-center">
                    {profileData.profilePicture ? (
                      <img 
                        src={profileData.profilePicture} 
                        alt="Profile" 
                        className="w-full h-full rounded-full object-cover"
                      />
                    ) : (
                      <User className="h-10 w-10 text-gray-400" />
                    )}
                  </div>
                  {isEditing && (
                    <button className="absolute bottom-0 right-0 p-1 bg-blue-600 rounded-full text-white hover:bg-blue-700">
                      <Camera className="h-3 w-3" />
                    </button>
                  )}
                </div>
                <div>
                  <h3 className="font-medium">{profileData.firstName} {profileData.lastName}</h3>
                  <p className="text-sm text-gray-600">@{profileData.username}</p>
                </div>
              </div>

              {/* Form Fields */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Username"
                  name="username"
                  value={profileData.username}
                  onChange={handleChange}
                  disabled={!isEditing}
                  error={errors.username}
                  icon={<User className="h-4 w-4" />}
                />

                <Input
                  label="Email"
                  name="email"
                  type="email"
                  value={profileData.email}
                  onChange={handleChange}
                  disabled={!isEditing}
                  error={errors.email}
                  icon={<Mail className="h-4 w-4" />}
                />

                <Input
                  label="First Name"
                  name="firstName"
                  value={profileData.firstName}
                  onChange={handleChange}
                  disabled={!isEditing}
                  error={errors.firstName}
                />

                <Input
                  label="Last Name"
                  name="lastName"
                  value={profileData.lastName}
                  onChange={handleChange}
                  disabled={!isEditing}
                  error={errors.lastName}
                />

                <Input
                  label="Phone"
                  name="phone"
                  value={profileData.phone}
                  onChange={handleChange}
                  disabled={!isEditing}
                  error={errors.phone}
                  icon={<Phone className="h-4 w-4" />}
                />

                <Input
                  label="Date of Birth"
                  name="dateOfBirth"
                  type="date"
                  value={profileData.dateOfBirth}
                  onChange={handleChange}
                  disabled={!isEditing}
                  error={errors.dateOfBirth}
                  icon={<Calendar className="h-4 w-4" />}
                />
              </div>

              <Input
                label="Address"
                name="address"
                value={profileData.address}
                onChange={handleChange}
                disabled={!isEditing}
                error={errors.address}
                icon={<MapPin className="h-4 w-4" />}
              />

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Bio</label>
                <textarea
                  name="bio"
                  value={profileData.bio}
                  onChange={handleChange}
                  disabled={!isEditing}
                  rows="3"
                  placeholder="Tell us about yourself..."
                  className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                    !isEditing ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'
                  } ${errors.bio ? 'border-red-500' : 'border-gray-300'}`}
                />
                {errors.bio && <p className="mt-1 text-sm text-red-600">{errors.bio}</p>}
              </div>

              {errors.save && (
                <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
                  {errors.save}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        {!loading && (
          <div className="sticky bottom-0 bg-white border-t p-4">
            <div className="flex justify-between items-center">
              <div className="text-sm text-gray-600">
                {isEditing && hasChanges() && (
                  <span className="text-yellow-600">You have unsaved changes</span>
                )}
              </div>
              <div className="flex space-x-3">
                {!isEditing ? (
                  <Button onClick={() => setIsEditing(true)}>
                    <Edit className="h-4 w-4 mr-2" />
                    Edit Profile
                  </Button>
                ) : (
                  <>
                    <Button variant="secondary" onClick={handleCancel}>
                      Cancel
                    </Button>
                    <Button 
                      onClick={handleSave} 
                      disabled={saving || !hasChanges()}
                    >
                      {saving ? (
                        <>
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          Saving...
                        </>
                      ) : (
                        <>
                          <Save className="h-4 w-4 mr-2" />
                          Save Changes
                        </>
                      )}
                    </Button>
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProfileModal;