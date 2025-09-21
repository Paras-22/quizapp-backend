// Enhanced Input.js component with icon support
import React from 'react';

const Input = ({ 
  label, 
  error, 
  type = 'text',
  placeholder = '',
  className = '',
  icon,
  disabled = false,
  ...props 
}) => (
  <div className={`mb-4 ${className}`}>
    {label && (
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
    )}
    <div className="relative">
      {icon && (
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <div className="text-gray-400">
            {icon}
          </div>
        </div>
      )}
      <input
        type={type}
        placeholder={placeholder}
        disabled={disabled}
        className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
          icon ? 'pl-10' : ''
        } ${
          disabled ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'
        } ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        {...props}
      />
    </div>
    {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
  </div>
);

export default Input;