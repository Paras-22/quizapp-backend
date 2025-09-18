
// src/components/ui/Card.js
import React from 'react';

const Card = ({ children, className = '', hover = false }) => (
  <div className={`bg-white rounded-lg shadow-md p-6 ${hover ? 'card-hover' : ''} ${className}`}>
    {children}
  </div>
);

export default Card;
