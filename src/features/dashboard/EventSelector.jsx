import React, { useState } from "react";
import { Plus, ArrowRight, KeyRound, MapPin, Calendar } from "lucide-react";

const EventSelector = ({
  events,
  onSelectEvent,
  onCreateNew,
  onRefreshList,
}) => {
  const [joinCode, setJoinCode] = useState("");
  const [isJoining, setIsJoining] = useState(false);

  const handleJoinEvent = async (e) => {
    e.preventDefault();
    setIsJoining(true);
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/events/join?joinCode=${joinCode}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (response.ok) {
        alert("Pomyślnie dołączono do nowego wydarzenia!");
        setJoinCode("");

        if (typeof onRefreshList === "function") {
          await onRefreshList();
        }
      } else {
        const errText = await response.text();
        alert("Błąd dołączania: " + (errText || "Błędny kod zaproszenia"));
      }
    } catch (err) {
      alert("Błąd sieciowy: " + err.message);
    } finally {
      setIsJoining(false);
    }
  };

  return (
    <div className="container py-5 text-center animate-fade-in">
      {/* NAGŁÓWEK */}
      <div className="mb-5">
        <div className="d-inline-flex align-items-center justify-content-center p-3 bg-success-light rounded-circle text-emerald mb-3">
          <svg
            width="32"
            height="32"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </div>
        <h2 className="fw-bold text-dark display-6 mb-2">Twoje Wydarzenia</h2>
        <p className="text-muted">Wybierz wyjazd, który chcesz rozliczyć</p>
      </div>

      {/* GRID Z KAFELKAMI */}
      <div className="row g-4 justify-content-center align-items-stretch">
        {/* LISTA ISTNIEJĄCYCH WYDARZEŃ */}
        {events.map((event) => {
          const formattedDate = event.date
            ? new Date(event.date).toLocaleDateString("pl-PL", {
                day: "numeric",
                month: "long",
                year: "numeric",
              })
            : "Brak ustalonej daty";

          return (
            <div key={event.id} className="col-md-4 col-sm-6">
              <div
                className="card h-100 p-4 shadow-sm border-0 rounded-4 text-start position-relative cursor-pointer event-select-card"
                onClick={() => onSelectEvent(event.id)}
                style={{
                  transition: "transform 0.2s, box-shadow 0.2s",
                  backgroundColor: "#fff",
                }}
              >
                <div className="d-flex justify-content-between align-items-start mb-3">
                  <h4 className="fw-bold text-dark m-0 text-truncate pe-3">
                    {event.title}
                  </h4>
                  <ArrowRight size={18} className="text-muted arrow-icon" />
                </div>

                {/* Szczegóły: Miejsce docelowe */}
                <div className="d-flex align-items-center gap-2 text-muted small mb-2">
                  <MapPin size={14} className="text-emerald" />
                  <span className="text-truncate">
                    {event.location || "Polska"}
                  </span>
                </div>

                {/* Szczegóły: Dokładna data z rokiem */}
                <div className="d-flex align-items-center gap-2 text-muted small mb-4">
                  <Calendar size={14} className="text-emerald" />
                  <span>{formattedDate}</span>
                </div>

                {/* Dolna belka podsumowania (Zależna od statusu zamknięcia) */}
                <div className="mt-auto pt-2 border-top">
                  <div
                    className="d-flex justify-content-between text-muted"
                    style={{ fontSize: "11px" }}
                  >
                    {/* DYNAMICZNY BADGE */}
                    <span
                      className={`badge text-uppercase fw-bold rounded-pill px-2 py-1 ${
                        event.closed
                          ? "bg-secondary text-white"
                          : "bg-success-light text-success"
                      }`}
                      style={{ fontSize: "9px" }}
                    >
                      {event.closed ? "Zamknięte" : "Aktywne"}
                    </span>
                    <span className="fw-medium text-secondary">
                      {event.type || "Wyjazd"}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          );
        })}

        {/* KAFELEK 2: DODAWANIE NOWEGO */}
        <div className="col-md-4 col-sm-6">
          <div
            className="card h-100 p-4 rounded-4 d-flex flex-column align-items-center justify-content-center text-center cursor-pointer"
            onClick={onCreateNew}
            style={{
              border: "2px dashed #dee2e6",
              backgroundColor: "#f8f9fa",
              minHeight: "200px",
              transition: "background-color 0.2s",
            }}
          >
            <Plus size={32} className="text-muted mb-2" />
            <span className="fw-bold text-secondary small">
              Dodaj nowe wydarzenie
            </span>
          </div>
        </div>

        {/* KAFELEK 3: DOŁĄCZANIE KODEM */}
        <div className="col-md-4 col-sm-6">
          <div
            className="card h-100 p-4 rounded-4 d-flex flex-column justify-content-center text-start shadow-sm border-0"
            style={{ backgroundColor: "#fff" }}
          >
            <div className="d-flex align-items-center gap-2 mb-2 text-dark">
              <KeyRound size={20} className="text-emerald" />
              <span
                className="fw-bold small text-uppercase"
                style={{ letterSpacing: "0.5px" }}
              >
                Dołącz kodem
              </span>
            </div>
            <p className="text-muted" style={{ fontSize: "12px" }}>
              Znajomy założył już wyjazd? Wklej kod, aby wskoczyć do grupy.
            </p>

            <form onSubmit={handleJoinEvent} className="mt-auto">
              <div className="input-group">
                <input
                  type="text"
                  className="form-control form-control-sm"
                  placeholder="Kod zaproszenia..."
                  value={joinCode}
                  onChange={(e) => setJoinCode(e.target.value)}
                  required
                  style={{ borderRadius: "8px 0 0 8px", fontSize: "13px" }}
                />
                <button
                  type="submit"
                  className="btn btn-emerald btn-sm px-3 fw-bold"
                  disabled={isJoining}
                  style={{ borderRadius: "0 8px 8px 0" }}
                >
                  {isJoining ? "..." : "Ok"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EventSelector;
