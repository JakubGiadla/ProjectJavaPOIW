import React, { useState } from "react";
import {
  Wallet,
  CheckSquare,
  Users,
  X,
  Scale,
  Percent,
  Hash,
} from "lucide-react";
import "./Dashboard.css";

const Dashboard = ({
  eventData,
  onTaskAction,
  onAddExpense,
  onAddTask,
  currentUser,
}) => {
  // Stan dla formularza ZADAŃ
  const [isAddingTask, setIsAddingTask] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState("");
  const [taskAssignedTo, setTaskAssignedTo] = useState(null);

  // Stan dla formularza WYDATKÓW
  const [isAddingExpense, setIsAddingExpense] = useState(false);
  const [expAmount, setExpAmount] = useState("");
  const [expTitle, setExpTitle] = useState("");
  const [splitMode, setSplitMode] = useState("equal");

  // Dynamiczna aktualizacja zaznaczonych użytkowników z bazy pod Springowe "attendees"
  const [selectedUsers, setSelectedUsers] = useState([]);

  if (!eventData)
    return <div className="container py-4 text-center">Ładowanie...</div>;

  // Czyste wartości finansowe - jeśli nie ma ich w JSON-ie z bazy, ląduje równe 0
  const collectedAmount = eventData.collected || 0;
  const goalAmount = eventData.goal || 0;

  const handleSaveTask = (e) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;
    onAddTask(newTaskTitle, taskAssignedTo);
    setNewTaskTitle("");
    setTaskAssignedTo(null);
    setIsAddingTask(false);
  };

  // ORYGINALNA METODA CHŁOPAKÓW - PRZYWRÓCONA 1:1
  const handleSaveExpense = (e) => {
    e.preventDefault();
    if (!expAmount || !expTitle) return;
    onAddExpense(expAmount, expTitle);
    setExpAmount("");
    setExpTitle("");
    setIsAddingExpense(false);
  };

  const toggleUserSelection = (userId) => {
    setSelectedUsers((prev) =>
      prev.includes(userId)
        ? prev.filter((id) => id !== userId)
        : [...prev, userId],
    );
  };

  return (
    <div className="container py-4 animate-fade-in">
      {/* SEKCJA HERO */}
      <div className="hero-counter shadow-sm bg-white text-center p-5 rounded-4 mb-4">
        <p className="text-uppercase fw-bold text-muted mb-1 small">
          Wydarzenie {eventData.type ? `(${eventData.type})` : ""}
        </p>
        <h1 className="fw-bold text-emerald">{eventData.title}</h1>
        {eventData.location && (
          <p className="text-muted small mb-3">
            Lokalizacja: {eventData.location}
          </p>
        )}
        <div className="progress-section mx-auto" style={{ maxWidth: "500px" }}>
          <div className="custom-progress-bar">
            <div
              className="progress-fill"
              style={{
                width: `${Math.min(100, Math.round((collectedAmount / (goalAmount || 1)) * 100))}%`,
              }}
            ></div>
          </div>
          <p className="text-muted small mt-2 fw-medium">
            Wykorzystano {collectedAmount.toLocaleString()} zł z{" "}
            {goalAmount.toLocaleString()} zł
          </p>
        </div>
      </div>

      <div className="row g-4 mb-5">
        {/* KARTA: KASA */}
        <div className="col-md-4">
          <div className="card h-100 p-4 shadow-sm border-0 rounded-4 d-flex flex-column">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="fw-bold m-0">Kasa</h5>
              <Wallet className="text-emerald" size={24} />
            </div>

            <div
              className="expense-list"
              style={{ flex: 1, overflowY: "auto" }}
            >
              {eventData.expenses && eventData.expenses.length > 0 ? (
                eventData.expenses.map((exp) => {
                  const participant = eventData.attendees?.find(
                    (p) => p.userId === exp.userId,
                  );
                  const displayName = participant
                    ? participant.name
                    : exp.user || "Uczestnik";

                  return (
                    <div
                      key={exp.id}
                      className="d-flex justify-content-between small py-2 border-bottom"
                    >
                      <span className="text-muted text-truncate">
                        {displayName}: {exp.title}
                      </span>
                      <span
                        className={
                          exp.amount < 0 ? "text-danger" : "text-success"
                        }
                      >
                        {exp.amount} zł
                      </span>
                    </div>
                  );
                })
              ) : (
                <p className="text-muted small text-center my-4">
                  Brak wydatków (Generuj raport w API)
                </p>
              )}
            </div>

            <div className="mt-3 pt-3 border-top">
              {!isAddingExpense ? (
                <button
                  className="btn btn-emerald w-100 py-2 fw-bold"
                  onClick={() => {
                    if (eventData?.attendees) {
                      setSelectedUsers(
                        eventData.attendees.map((p) => p.userId),
                      );
                    }
                    setIsAddingExpense(true);
                  }}
                >
                  + Dodaj wydatek
                </button>
              ) : (
                <div className="inline-form animate-fade-in">
                  <input
                    type="number"
                    className="form-control-custom mb-2"
                    placeholder="0.00 zł"
                    value={expAmount}
                    onChange={(e) => setExpAmount(e.target.value)}
                    autoFocus
                  />
                  <input
                    type="text"
                    className="form-control-custom mb-3"
                    placeholder="Za co?"
                    value={expTitle}
                    onChange={(e) => setExpTitle(e.target.value)}
                  />

                  <label className="text-muted small fw-bold text-uppercase mb-2 d-block">
                    Jak dzielimy?
                  </label>
                  <div className="split-options-mini mb-3">
                    <div
                      className={`split-pill ${splitMode === "equal" ? "active" : ""}`}
                      onClick={() => setSplitMode("equal")}
                    >
                      <Scale size={14} />
                    </div>
                    <div
                      className={`split-pill ${splitMode === "percent" ? "active" : ""}`}
                      onClick={() => setSplitMode("percent")}
                    >
                      <Percent size={14} />
                    </div>
                    <div
                      className={`split-pill ${splitMode === "amount" ? "active" : ""}`}
                      onClick={() => setSplitMode("amount")}
                    >
                      <Hash size={14} />
                    </div>
                  </div>

                  <label className="text-muted small fw-bold text-uppercase mb-2 d-block">
                    Kogo dotyczy?
                  </label>
                  <div className="participants-scroll-mini mb-3">
                    {eventData.attendees &&
                      eventData.attendees.map((p) => (
                        <div
                          key={p.userId}
                          className="participant-mini-item"
                          onClick={() => toggleUserSelection(p.userId)}
                        >
                          <input
                            type="checkbox"
                            checked={selectedUsers.includes(p.userId)}
                            readOnly
                            className="custom-checkbox-sm"
                          />
                          <span className="small ms-2">
                            {p.name || "Nieznany"}
                          </span>
                        </div>
                      ))}
                  </div>

                  <div className="d-flex gap-2">
                    <button
                      className="btn btn-emerald flex-grow-1"
                      onClick={handleSaveExpense}
                    >
                      Zapisz
                    </button>
                    <button
                      className="btn btn-light"
                      onClick={() => setIsAddingExpense(false)}
                    >
                      <X size={18} />
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* KARTA: ZADANIA */}
        <div className="col-md-4">
          <div className="card h-100 p-4 shadow-sm border-emerald rounded-4 d-flex flex-column">
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h5 className="fw-bold m-0">Zadania</h5>
              <CheckSquare className="text-emerald" size={24} />
            </div>

            <div className="task-list" style={{ flex: 1, overflowY: "auto" }}>
              {eventData.tasks &&
                eventData.tasks.map((task) => {
                  const isTaskDone =
                    task.isDone !== undefined ? task.isDone : task.isCompleted;

                  let displayAssignee = "?";
                  if (task.assignedTo) {
                    displayAssignee = task.assignedTo;
                  } else if (task.assignee && task.assignee.name) {
                    displayAssignee = task.assignee.name
                      .charAt(0)
                      .toUpperCase();
                  }

                  const isAssignedToMe =
                    currentUser?.id &&
                    (task.assignedUserId === currentUser.id ||
                      task.assignee?.email === currentUser.email);

                  return (
                    <div
                      key={task.id}
                      className="task-row d-flex justify-content-between align-items-center py-2 border-bottom"
                    >
                      <div className="d-flex align-items-center gap-2">
                        <input
                          type="checkbox"
                          checked={isTaskDone || false}
                          readOnly
                          className="custom-checkbox"
                        />
                        <span
                          className={`task-label ${isTaskDone ? "task-done" : ""}`}
                        >
                          {task.title || task.description}
                        </span>
                      </div>
                      <div
                        className={`avatar-sm-circle bg-info text-white ${
                          displayAssignee !== "?"
                            ? isAssignedToMe
                              ? "hover-danger-action"
                              : "avatar-locked"
                            : "bg-light text-muted"
                        }`}
                        onClick={() => onTaskAction && onTaskAction(task.id)}
                      >
                        {displayAssignee}
                      </div>
                    </div>
                  );
                })}
            </div>

            <div className="mt-3 pt-3 border-top">
              {!isAddingTask ? (
                <button
                  className="btn btn-emerald w-100 py-2 fw-bold"
                  onClick={() => setIsAddingTask(true)}
                >
                  + Nowe zadanie
                </button>
              ) : (
                <form onSubmit={handleSaveTask} className="animate-fade-in">
                  <input
                    type="text"
                    className="form-control-custom mb-3"
                    placeholder="Co dopisać?"
                    value={newTaskTitle}
                    onChange={(e) => setNewTaskTitle(e.target.value)}
                    autoFocus
                    required
                  />
                  <label className="text-muted small fw-bold text-uppercase mb-2 d-block">
                    Kto to zrobi?
                  </label>
                  <div className="d-flex gap-2 mb-3 overflow-x-auto py-1">
                    <div
                      className={`avatar-sm-circle border cursor-pointer ${taskAssignedTo === null ? "border-emerald" : "bg-light"}`}
                      onClick={() => setTaskAssignedTo(null)}
                    >
                      ?
                    </div>
                    {eventData.attendees &&
                      eventData.attendees.map((p) => (
                        <div
                          key={p.userId}
                          className={`avatar-sm-circle cursor-pointer ${p.color || "bg-success"} ${taskAssignedTo === p.userId ? "ring-emerald" : ""}`}
                          onClick={() => setTaskAssignedTo(p.userId)}
                        >
                          {p.avatar || (p.name ? p.name.charAt(0) : "U")}
                        </div>
                      ))}
                  </div>
                  <div className="d-flex gap-2">
                    <button
                      type="submit"
                      className="btn btn-emerald flex-grow-1"
                    >
                      Dodaj
                    </button>
                    <button
                      type="button"
                      className="btn btn-light"
                      onClick={() => setIsAddingTask(false)}
                    >
                      <X size={18} />
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>

        {/* KARTA: LUDZIE */}
        <div className="col-md-4">
          <div className="card h-100 p-4 shadow-sm border-0 rounded-4 d-flex flex-column">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="fw-bold m-0">Ludzie</h5>
              <Users className="text-emerald" size={24} />
            </div>
            <div className="people-list" style={{ flex: 1, overflowY: "auto" }}>
              {eventData.attendees &&
                eventData.attendees.map((person) => (
                  <div
                    key={person.userId}
                    className="d-flex align-items-center justify-content-between mb-3"
                  >
                    <div className="d-flex align-items-center">
                      <div
                        className={`avatar-sm-circle bg-success text-white me-2`}
                      >
                        {person.name
                          ? person.name.charAt(0).toUpperCase()
                          : "U"}
                      </div>
                      <span className="small fw-medium">
                        {person.name || "Uczestnik"}
                      </span>
                    </div>
                    <span
                      className="badge bg-light text-muted"
                      style={{ fontSize: "10px" }}
                    >
                      {eventData.organizerName === person.name
                        ? "Organizator"
                        : "Uczestnik"}
                    </span>
                  </div>
                ))}
            </div>
            {eventData.joinCode && (
              <div className="text-center my-2 p-2 bg-light rounded-3">
                <small
                  className="text-muted d-block"
                  style={{ fontSize: "10px" }}
                >
                  KOD ZAPROSZENIA
                </small>
                <span className="fw-bold text-dark small">
                  {eventData.joinCode}
                </span>
              </div>
            )}
            <button className="btn btn-light w-100 py-2 fw-bold mt-2 text-muted">
              Kopiuj link
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
