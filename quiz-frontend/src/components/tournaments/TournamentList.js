// src/components/tournaments/TournamentList.js
import React from "react";

const TournamentList = ({ tournaments, onEdit, onDelete }) => {
  return (
    <table className="table-auto w-full border">
      <thead>
        <tr>
          <th className="px-4 py-2">Creator</th>
          <th className="px-4 py-2">Name</th>
          <th className="px-4 py-2">Category</th>
          <th className="px-4 py-2">Difficulty</th>
          <th className="px-4 py-2">Actions</th>
        </tr>
      </thead>
      <tbody>
        {tournaments.map((tournament) => (
          <tr key={tournament.id}>
            <td className="border px-4 py-2">{tournament.creator}</td>
            <td className="border px-4 py-2">{tournament.name}</td>
            <td className="border px-4 py-2">{tournament.category}</td>
            <td className="border px-4 py-2">{tournament.difficulty}</td>
            <td className="border px-4 py-2 space-x-2">
              <button
                onClick={() => onEdit(tournament)}
                className="bg-yellow-500 text-white px-2 py-1 rounded"
              >
                Edit
              </button>
              <button
                onClick={() => onDelete(tournament.id)}
                className="bg-red-500 text-white px-2 py-1 rounded"
              >
                Delete
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TournamentList;
