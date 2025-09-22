// App.js - Main application entry point with routing and authentication

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './components/auth/LoginPage';
import Layout from './components/layout/Layout';
import AdminDashboard from './components/dashboard/AdminDashboard';
import PlayerDashboard from './components/dashboard/PlayerDashboard';
import QuizPlayer from './components/tournaments/QuizPlayer';

// ProtectedRoute component ensures only authenticated users can access certain routes
const ProtectedRoute = ({ children, adminOnly = false }) => {
  const { user, loading } = useAuth();

  // Show loading spinner while authentication state is being determined
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  // Redirect unauthenticated users to login page
  if (!user) {
    return <Navigate to="/login" />;
  }

  // Redirect non-admin users if route is restricted to admins
  if (adminOnly && user.role !== 'ADMIN') {
    return <Navigate to="/dashboard" />;
  }

  // Render protected content
  return children;
};

// AppRoutes defines all application routes based on authentication and role
const AppRoutes = () => {
  const { user } = useAuth();

  // If user is not authenticated, show login page
  if (!user) {
    return <LoginPage />;
  }

  return (
    <Layout>
      <Routes>
        {/* Redirect root path to dashboard */}
        <Route 
          path="/" 
          element={<Navigate to="/dashboard" replace />} 
        />

        {/* Dashboard route: shows Admin or Player dashboard based on role */}
        <Route 
          path="/dashboard" 
          element={
            <ProtectedRoute>
              {user.role === 'ADMIN' ? <AdminDashboard /> : <PlayerDashboard />}
            </ProtectedRoute>
          } 
        />

        {/* Quiz route: accessible to authenticated users */}
        <Route 
          path="/quiz/:tournamentId" 
          element={
            <ProtectedRoute>
              <QuizPlayer />
            </ProtectedRoute>
          } 
        />

        {/* Catch-all route: redirect unknown paths to dashboard */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Layout>
  );
};

// App component wraps everything in Router and AuthProvider
function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          <AppRoutes />
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;
