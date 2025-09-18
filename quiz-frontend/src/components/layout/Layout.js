// src/components/layout/Layout.js
import React from 'react';
import Navigation from '../ui/Navigation';

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <main className="py-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;