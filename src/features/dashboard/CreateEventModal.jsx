import React, { useState } from "react";
import { X, PartyPopper } from "lucide-react";
import "./CreateEventModal.css";

const CreateEventModal = ({ isOpen, onClose, onCreate }) => {
  const [title, setTitle] = useState("");
  const [location, setLocation] = useState("");
  const [date, setDate] = useState("");
  const [maxAttendees, setMaxAttendees] = useState("10"); // Domyślnie 10 osób, tak jak w encji
  const [type, setType] = useState("Wyjazd"); // Domyślny typ wyjazdu

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) return alert("Podaj nazwę wydarzenia!");

    // Przekazujemy TYLKO te parametry, które fizycznie istnieją w Event.java na backendzie
    onCreate({
      title: title.trim(),
      location: location.trim() || "Zakopane",
      date: date || null,
      maxAttendees: Number(maxAttendees) || 10,
      type: type,
    });

    setTitle("");
    setLocation("");
    setDate("");
    setMaxAttendees("10");
    setType("Wyjazd");
    onClose();
  };

  return (
    <div className="cem-overlay" onClick={onClose}>
      <div className="cem-container" onClick={(e) => e.stopPropagation()}>
        <div className="cem-header">
          <h5 className="cem-title">Nowe wydarzenie</h5>
          <button className="cem-close-btn" onClick={onClose}>
            <X size={22} />
          </button>
        </div>

        <div className="cem-body">
          <form onSubmit={handleSubmit}>
            <div className="text-center mb-4">
              <PartyPopper size={56} style={{ color: "#10b981" }} />
            </div>

            {/* NAZWA WYDARZENIA */}
            <div className="cem-form-group">
              <label className="cem-label">Nazwa wydarzenia *</label>
              <input
                type="text"
                className="cem-input"
                placeholder="np. Narty we Włoszech"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                autoFocus
                required
              />
            </div>

            {/* LOKALIZACJA - Dopasowana do pola z bazy */}
            <div className="cem-form-group">
              <label className="cem-label">Lokalizacja</label>
              <input
                type="text"
                className="cem-input"
                placeholder="np. Zakopane, Włochy"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              />
            </div>

            {/* DATA */}
            <div className="cem-form-group">
              <label className="cem-label">Data wydarzenia</label>
              <input
                type="date"
                className="cem-input"
                value={date}
                onChange={(e) => setDate(e.target.value)}
              />
            </div>

            {/* MAKSYMALNA LICZBA UCZESTNIKÓW - Dopasowana do pola maxAttendees z bazy */}
            <div className="cem-form-group">
              <label className="cem-label">Limit osób (Uczestnicy)</label>
              <input
                type="number"
                className="cem-input"
                placeholder="np. 10"
                value={maxAttendees}
                onChange={(e) => setMaxAttendees(e.target.value)}
              />
            </div>

            {/* TYP WYDARZENIA */}
            <div className="cem-form-group">
              <label className="cem-label">Typ wydarzenia</label>
              <select
                className="cem-input"
                value={type}
                onChange={(e) => setType(e.target.value)}
                style={{ backgroundColor: "#fff" }}
              >
                <option value="Wyjazd">Wyjazd</option>
                <option value="Impreza">Impreza</option>
                <option value="Zbiórka">Zbiórka</option>
                <option value="Inne">Inne</option>
              </select>
            </div>

            <button type="submit" className="cem-submit-btn">
              Utwórz wydarzenie
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateEventModal;
