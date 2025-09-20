// src/components/tournaments/TournamentForm.js
import React, { useState } from "react";
import { CATEGORIES, DIFFICULTY_LEVELS } from "../../utils/constants";

const TournamentForm = ({ initialData = {}, onSubmit, onClose }) => {
  const [form, setForm] = useState({
    creator: initialData.creator || "",
    name: initialData.name || "",
    category: initialData.category || "",
    difficulty: initialData.difficulty || ""
  });

  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validate = () => {
    const newErrors = {};
    if (!form.creator) newErrors.creator = "Creator is required";
    if (!form.name) newErrors.name = "Name is required";
    if (!form.category) newErrors.category = "Category is required";
    if (!form.difficulty) newErrors.difficulty = "Difficulty is required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit(form);
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40">
      <div className="bg-white p-6 rounded shadow w-96">
        <h3 className="text-xl font-bold mb-4">
          {initialData.id ? "Edit Tournament" : "Create Tournament"}
        </h3>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="block">Creator</label>
            <input
              type="text"
              name="creator"
              value={form.creator}
              onChange={handleChange}
              className="border w-full p-2"
            />
            {errors.creator && <p className="text-red-500">{errors.creator}</p>}
          </div>

          <div className="mb-3">
            <label className="block">Name</label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              className="border w-full p-2"
            />
            {errors.name && <p className="text-red-500">{errors.name}</p>}
          </div>

          <div className="mb-3">
            <label className="block">Category</label>
            <select
              name="category"
              value={form.category}
              onChange={handleChange}
              className="border w-full p-2"
            >
              <option value="">Select Category</option>
              {CATEGORIES.map((cat) => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
            {errors.category && <p className="text-red-500">{errors.category}</p>}
          </div>

          <div className="mb-3">
            <label className="block">Difficulty</label>
            <select
              name="difficulty"
              value={form.difficulty}
              onChange={handleChange}
              className="border w-full p-2"
            >
              <option value="">Select Difficulty</option>
              {DIFFICULTY_LEVELS.map((level) => (
                <option key={level} value={level}>{level}</option>
              ))}
            </select>
            {errors.difficulty && <p className="text-red-500">{errors.difficulty}</p>}
          </div>

          <div className="flex justify-end space-x-2">
            <button
              type="button"
              onClick={onClose}
              className="bg-gray-400 text-white px-4 py-2 rounded"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded"
            >
              {initialData.id ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TournamentForm;
