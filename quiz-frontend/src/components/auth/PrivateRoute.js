// src/components/auth/PrivateRoute.js
import React, { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";

/**
 * PrivateRoute component
 * @param {JSX.Element} children - The component to render if authorized
 * @param {Array} roles - Allowed roles for this route
 */
const PrivateRoute = ({ children, roles }) => {
  const { user } = useContext(AuthContext);

  if (!user) {
    // Not logged in → redirect to login
    return <Navigate to="/login" />;
  }

  if (roles && !roles.includes(user.role)) {
    // Logged in but not authorized → redirect based on role
    return user.role === "ADMIN" ? (
      <Navigate to="/admin" />
    ) : (
      <Navigate to="/player" />
    );
  }

  return children;
};

export default PrivateRoute;
