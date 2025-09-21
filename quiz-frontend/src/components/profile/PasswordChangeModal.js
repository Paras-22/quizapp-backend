// PasswordChangeModal.js - Modal for changing password
import React, { useState } from 'react';
import { X, Lock, Eye, EyeOff } from 'lucide-react';
import Button from '../ui/Button';
import Input from '../ui/Input';
import { apiService } from '../../services/api';

const PasswordChangeModal = ({ isOpen, onClose }) => {
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false
  });
  
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const validateForm = () => {
    const newErrors = {};
    
    if (!passwordData.currentPassword) {
      newErrors.currentPassword = 'Current password is required';
    }
    
    if (!passwordData.newPassword) {
      newErrors.newPassword = 'New password is required';
    } else if (passwordData.newPassword.length < 6) {
      newErrors.newPassword = 'Password must be at least 6 characters';
    }
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    
    if (passwordData.currentPassword === passwordData.newPassword) {
      newErrors.newPassword = 'New password must be different from current password';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setLoading(true);
    setErrors({});
    
    try {
      await apiService.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      
      setSuccess(true);
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
      
      setTimeout(() => {
        setSuccess(false);
        onClose();
      }, 2000);
      
    } catch (error) {
      setErrors({ submit: 'Failed to change password. Please check your current password.' });
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({ ...prev, [name]: value }));
    
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const togglePasswordVisibility = (field) => {
    setShowPasswords(prev => ({ ...prev, [field]: !prev[field] }));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-md w-full">
        <div className="border-b p-6 flex justify-between items-center">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-red-100 rounded-full">
              <Lock className="h-5 w-5 text-red-600" />
            </div>
            <div>
              <h2 className="text-lg font-semibold">Change Password</h2>
              <p className="text-sm text-gray-600">Update your account password</p>
            </div>
          </div>
          <Button variant="outline" size="sm" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-6">
          {success && (
            <div className="p-4 bg-green-100 border border-green-400 text-green-700 rounded-lg mb-4">
              Password changed successfully!
            </div>
          )}

          <div className="space-y-4">
            <div className="relative">
              <Input
                label="Current Password"
                name="currentPassword"
                type={showPasswords.current ? 'text' : 'password'}
                value={passwordData.currentPassword}
                onChange={handleChange}
                error={errors.currentPassword}
                icon={<Lock className="h-4 w-4" />}
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('current')}
                className="absolute right-3 top-8 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.current ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>

            <div className="relative">
              <Input
                label="New Password"
                name="newPassword"
                type={showPasswords.new ? 'text' : 'password'}
                value={passwordData.newPassword}
                onChange={handleChange}
                error={errors.newPassword}
                icon={<Lock className="h-4 w-4" />}
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('new')}
                className="absolute right-3 top-8 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.new ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>

            <div className="relative">
              <Input
                label="Confirm New Password"
                name="confirmPassword"
                type={showPasswords.confirm ? 'text' : 'password'}
                value={passwordData.confirmPassword}
                onChange={handleChange}
                error={errors.confirmPassword}
                icon={<Lock className="h-4 w-4" />}
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('confirm')}
                className="absolute right-3 top-8 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.confirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>

            {errors.submit && (
              <div className="p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
                {errors.submit}
              </div>
            )}
          </div>

          <div className="flex space-x-3 mt-6">
            <Button type="submit" disabled={loading} className="flex-1">
              {loading ? 'Changing...' : 'Change Password'}
            </Button>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancel
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PasswordChangeModal;