// src/components/dashboard/AdminDashboard.js
import React, { useEffect, useState } from "react";
import { apiService } from "../../services/api";
import TournamentForm from "../tournaments/TournamentForm";
import TournamentList from "../tournaments/TournamentList";

const AdminDashboard = () => {
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editTournament, setEditTournament] = useState(null);

  // Fetch tournaments
  const fetchTournaments = async () => {
    try {
      setLoading(true);
      const data = await apiService.getTournaments();
      setTournaments(data);
    } catch (err) {
      setError("Failed to load tournaments");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTournaments();
  }, []);

  const handleCreate = async (tournamentData) => {
    try {
      await apiService.createTournament(tournamentData);
      setShowForm(false);
      fetchTournaments();
    } catch (err) {
      setError("Error creating tournament");
    }
  };

  const handleUpdate = async (id, updatedData) => {
    try {
      await apiService.updateTournament(id, updatedData);
      setEditTournament(null);
      fetchTournaments();
    } catch (err) {
      setError("Error updating tournament");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this tournament?")) return;
    try {
      await apiService.deleteTournament(id);
      fetchTournaments();
    } catch (err) {
      setError("Error deleting tournament");
    }
  };

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-4">Admin Dashboard</h2>

      {error && <p className="text-red-500">{error}</p>}
      {loading ? (
        <p>Loading tournaments...</p>
      ) : (
        <TournamentList
          tournaments={tournaments}
          onEdit={setEditTournament}
          onDelete={handleDelete}
        />
      )}

      <button
        onClick={() => setShowForm(true)}
        className="mt-4 bg-blue-600 text-white px-4 py-2 rounded"
      >
        + Create Tournament
      </button>

      {showForm && (
        <TournamentForm
          onSubmit={handleCreate}
          onClose={() => setShowForm(false)}
        />
      )}

      {editTournament && (
        <TournamentForm
          initialData={editTournament}
          onSubmit={(data) => handleUpdate(editTournament.id, data)}
          onClose={() => setEditTournament(null)}
        />
      )}
    </div>
  );
};

export default AdminDashboard;
