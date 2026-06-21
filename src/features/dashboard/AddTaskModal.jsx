import React, { useState } from "react";
import { X } from "lucide-react";
// Importujemy ten sam CSS, w którym naprawiliśmy modale
import "./AddExpense.css";

const AddTaskModal = ({ isOpen, onClose, onAddTask, participants = [] }) => {
  const [title, setTitle] = useState("");
  const [assignedTo, setAssignedTo] = useState(null); // null = nikt (do wzięcia)

  if (!isOpen) return null;

  const handleSave = (e) => {
    e.preventDefault();
    if (!title) return alert("Podaj treść zadania!");
    onAddTask(title, assignedTo);
    setTitle("");
    setAssignedTo(null);
    onClose();
  };

  return (
    <div className="custom-modal-overlay" onClick={onClose}>
      <div
        className="custom-modal-content animate-fade-in"
        onClick={(e) => e.stopPropagation()}
      >
        {/* NAGŁÓWEK */}
        <div className="custom-modal-header">
          <h5 className="fw-bold m-0">Nowe zadanie</h5>
          <button type="button" className="btn-close-modal" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        {/* ŚRODEK (SCROLLOWANY) */}
        <div className="custom-modal-body">
          <form onSubmit={handleSave}>
            <div className="mb-4 mt-2">
              <label className="text-muted small fw-bold text-uppercase">
                Co jest do zrobienia?
              </label>
              <input
                type="text"
                className="form-control-custom"
                placeholder="np. Kupić węgiel na grilla..."
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                autoFocus
                required
              />
            </div>

            <div className="mb-4">
              <label className="text-muted small fw-bold text-uppercase d-block mb-2">
                Kto to zrobi? (Opcjonalnie)
              </label>
              <div>
                {/* Opcja "Nikt" */}
                <div
                  className="participant-item cursor-pointer"
                  onClick={() => setAssignedTo(null)}
                >
                  <input
                    type="radio"
                    className="custom-checkbox"
                    checked={assignedTo === null}
                    readOnly
                  />
                  <div className="avatar-sm-circle bg-light text-muted ms-3 me-3">
                    ?
                  </div>
                  <span className="small fw-medium">
                    Nikt (Zgłoszę się później)
                  </span>
                </div>

                {/* Lista uczestników z wydarzenia */}
                {participants.map((person) => (
                  <div
                    className="participant-item cursor-pointer"
                    key={person.userId || person.id}
                    onClick={() => setAssignedTo(person.userId)}
                  >
                    <input
                      type="radio"
                      className="custom-checkbox"
                      checked={assignedTo === person.userId}
                      readOnly
                    />
                    <div
                      className={`avatar-sm-circle ${person.color || "bg-light"} ms-3 me-3`}
                    >
                      {person.initial || person.name?.charAt(0).toUpperCase()}
                    </div>
                    <span className="small fw-medium">{person.name}</span>
                  </div>
                ))}
              </div>
            </div>

            <button
              type="submit"
              className="btn-emerald-modal w-100 py-3 fw-bold"
            >
              Dodaj zadanie
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AddTaskModal;
