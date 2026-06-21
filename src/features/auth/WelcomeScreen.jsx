import React, { useState } from "react";
import { PlusCircle, Link2 } from "lucide-react";

const WelcomeScreen = ({ onCreateEvent, onRefreshAllEvents }) => {
  const [joinCode, setJoinCode] = useState("");
  const [isJoining, setIsJoining] = useState(false);

  const handleJoinEvent = async (e) => {
    e.preventDefault();
    if (!joinCode.trim()) return;

    setIsJoining(true);
    try {
      const token = localStorage.getItem("token");
      // Strzał sieciowy do kontrolera Spring Boota
      const response = await fetch(
        `http://localhost:8080/api/events/join?joinCode=${joinCode.trim()}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (response.ok) {
        alert("Pomyślnie dołączono do wydarzenia!");
        setJoinCode("");
        // Wywołujemy callback z App.jsx, żeby odświeżył listę i przełączył nas na nowo dodany event
        if (onRefreshAllEvents) onRefreshAllEvents();
      } else {
        const errText = await response.text();
        alert("Błąd dołączania: " + (errText || "Błędny kod"));
      }
    } catch (err) {
      alert("Błąd sieci: " + err.message);
    } finally {
      setIsJoining(false);
    }
  };

  return (
    <div
      className="container py-5 text-center animate-fade-in"
      style={{ maxWidt: "500px" }}
    >
      <div className="card shadow p-5 border-0 rounded-4 bg-white">
        <h2 className="fw-bold mb-3 text-dark">Witaj w EventSplit!</h2>
        <p className="text-muted small mb-5">
          Nie uczestniczysz obecnie w żadnym wyjeździe. Stwórz własny plan lub
          dołącz do znajomych.
        </p>

        {/* OPCJA 1: TWORZENIE NOWEGO */}
        <button
          className="btn btn-emerald w-100 py-3 fw-bold mb-4 d-flex align-items-center justify-content-center gap-2 shadow-sm"
          onClick={onCreateEvent}
        >
          <PlusCircle size={20} /> Stwórz nowe wydarzenie
        </button>

        <div className="position-relative my-4 text-center">
          <hr className="text-muted" />
          <span className="position-absolute top-50 start-50 translate-middle bg-white px-3 text-muted small fw-bold text-uppercase">
            lub
          </span>
        </div>

        {/* OPCJA 2: DOŁĄCZANIE KODEM */}
        <form onSubmit={handleJoinEvent} className="text-start">
          <label className="form-label text-muted small fw-bold text-uppercase mb-2">
            Masz kod zaproszenia?
          </label>
          <div className="d-flex gap-2">
            <input
              type="text"
              className="form-control"
              placeholder="Wklej kod wyjazdu..."
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value)}
              required
              style={{ padding: "12px", borderRadius: "8px" }}
            />
            <button
              type="submit"
              className="btn btn-dark px-4 fw-bold"
              disabled={isJoining}
              style={{ borderRadius: "8px" }}
            >
              {isJoining ? "..." : "Dołącz"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default WelcomeScreen;
