// src/services/api.js
const API_BASE_URL = 'http://localhost:8080';

const getAuthHeaders = () => ({
  'Authorization': `Bearer ${localStorage.getItem('token')}`,
  'Content-Type': 'application/json'
});

const handleResponse = async (response) => {
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }
  return response.json();
};

export const apiService = {
  // -------------------
  // Tournament APIs
  // -------------------
  async getTournaments() {
    const response = await fetch(`${API_BASE_URL}/tournaments/all`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async createTournament(data) {
    const response = await fetch(`${API_BASE_URL}/tournaments/create`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  async updateTournament(id, data) {
    const response = await fetch(`${API_BASE_URL}/tournaments/update/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  async deleteTournament(id) {
    const response = await fetch(`${API_BASE_URL}/tournaments/delete/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async likeTournament(id) {
    const response = await fetch(`${API_BASE_URL}/tournaments/like/${id}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async getTournamentQuestions(tournamentId) {
    const response = await fetch(`${API_BASE_URL}/tournaments/${tournamentId}/questions`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async getScoreboard(id) {
    const response = await fetch(`${API_BASE_URL}/tournaments/${id}/scores`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // -------------------
  // Player APIs
  // -------------------
  async startTournament(id) {
    const response = await fetch(`${API_BASE_URL}/player/start/${id}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async submitAnswer(attemptId, questionId, data) {
    const response = await fetch(`${API_BASE_URL}/player/submit/${attemptId}/${questionId}`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  async finishTournament(attemptId) {
    const response = await fetch(`${API_BASE_URL}/player/finish/${attemptId}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async getMyAttempts() {
    const response = await fetch(`${API_BASE_URL}/player/my-attempts`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  async getPlayerStats() {
    const response = await fetch(`${API_BASE_URL}/users/stats`, {
      headers: getAuthHeaders()
    });
    return handleResponse(response);
  },

  // -------------------
  // Auth APIs
  // -------------------
  async register(data) {
    const response = await fetch(`${API_BASE_URL}/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  async login(data) {
    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  async requestPasswordReset(email) {
    const response = await fetch(`${API_BASE_URL}/users/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    return handleResponse(response);
  },

  async confirmPasswordReset(token, newPassword) {
    const response = await fetch(`${API_BASE_URL}/users/reset-password/confirm?token=${token}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ newPassword })
    });
    return handleResponse(response);
  }
};
