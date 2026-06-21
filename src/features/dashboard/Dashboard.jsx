import React, { useState, useEffect } from "react";
import {
  Wallet,
  CheckSquare,
  Users,
  X,
  Scale,
  Percent,
  Hash,
  MapPin,
  CalendarDays,
  Activity,
  ArrowRight,
  Lock,
  FileText,
} from "lucide-react";
import { useExpenseApi } from "../../hooks/useExpenseApi";
import { useTaskApi } from "../../hooks/useTaskApi";
import "./Dashboard.css";

const Dashboard = ({
  eventData,
  onTaskAction,
  onAddTask,
  currentUser,
  onRefreshEvent,
  onRefreshList,
}) => {
  const [isAddingTask, setIsAddingTask] = useState(false);
  const [newTaskTitle, setNewTitle] = useState("");

  const [isAddingExpense, setIsAddingExpense] = useState(false);
  const [expAmount, setExpAmount] = useState("");
  const [expTitle, setExpTitle] = useState("");
  const [splitMode, setSplitMode] = useState("equal");
  const [expenseValues, setExpenseValues] = useState({});

  const [fetchedExpenses, setFetchedExpenses] = useState([]);
  const [fetchedTasks, setFetchedTasks] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);

  const [showSettlements, setShowSettlements] = useState(false);
  const [settlementReport, setSettlementReport] = useState(null);

  // Lokalny stan wymuszający zamknięcie na froncie (dla natychmiastowej reakcji po 200 OK)
  const [localIsClosed, setLocalIsClosed] = useState(false);

  const { createExpenseOnServer } = useExpenseApi();
  const { createTaskOnServer } = useTaskApi();

  // Sprawdzamy aktywność wydarzenia na podstawie DTO z backendu (pole isClosed)
  const isEventActive = eventData?.isClosed !== true && !localIsClosed;

  // Sprawdzanie organizatora na podstawie organizerName z DTO
  const isOrganizer =
    currentUser?.name &&
    eventData?.organizerName &&
    currentUser.name.toLowerCase() === eventData.organizerName.toLowerCase();

  const loadExpensesReport = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/reports/${eventData.id}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (response.ok) {
        const reportData = await response.json();
        setFetchedExpenses(reportData.expenses || []);
        setSettlementReport(reportData);
      }
    } catch (err) {
      console.error("Nie udało się pobrać raportu wydatków:", err.message);
    }
  };

  useEffect(() => {
    if (eventData?.id) {
      loadExpensesReport();
      setFetchedTasks(eventData.tasks || []);
    }
    if (eventData?.attendees && eventData.attendees.length > 0) {
      setSelectedUsers(eventData.attendees.map((p) => p.userId));
    } else if (currentUser?.id) {
      setSelectedUsers([currentUser.id]);
    }
  }, [eventData, currentUser]);

  if (!eventData)
    return <div className="container py-4 text-center">Ładowanie...</div>;

  const displayAttendees = eventData.attendees || [];
  const totalTasksCount = fetchedTasks ? fetchedTasks.length : 0;
  const totalPeopleCount = displayAttendees.length;

  const totalExpensesSum =
    fetchedExpenses && fetchedExpenses.length > 0
      ? fetchedExpenses.reduce(
          (sum, exp) => sum + Math.abs(parseFloat(exp.amount) || 0),
          0,
        )
      : 0;

  const formattedDate = eventData.date
    ? new Date(eventData.date).toLocaleDateString("pl-PL", {
        day: "numeric",
        month: "long",
        year: "numeric",
      })
    : "Brak ustalonej daty";

  const handleCopyLink = () => {
    if (eventData?.joinCode) {
      navigator.clipboard.writeText(eventData.joinCode);
      alert("Kod zaproszenia skopiowany do schowka!");
    }
  };

  const handleCloseEvent = async () => {
    if (
      !window.confirm(
        "Czy na pewno chcesz zamknąć i rozliczyć ten wyjazd? Po zamknięciu nikt nie będzie mógł dodawać nowych kosztów i zadań!",
      )
    )
      return;
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/events/${eventData.id}/close`,
        {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (response.ok) {
        alert("Wyjazd został pomyślnie zamknięty i zamrożony!");
        setLocalIsClosed(true);

        if (onRefreshEvent) onRefreshEvent();

        // Automatycznie odświeżamy listę wszystkich wydarzeń w komponencie nadrzędnym (App.jsx)
        if (typeof onRefreshList === "function") {
          await onRefreshList();
        }
      } else {
        const errText = await response.text();
        alert("Nie udało się zamknąć wyjazdu: " + errText);
      }
    } catch (err) {
      alert("Błąd sieciowy przy zamykaniu wyjazdu: " + err.message);
    }
  };

  const handleAssignMeToTask = async (taskId) => {
    if (!isEventActive)
      return alert("Wyjazd jest zamknięty! Edycja zablokowana.");
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/events/${eventData.id}/tasks/${taskId}/assign?email=${currentUser.email}`,
        {
          method: "PUT",
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (response.ok) {
        setFetchedTasks((prev) =>
          prev.map((t) =>
            t.id === taskId ? { ...t, assignee: currentUser } : t,
          ),
        );
        if (onRefreshEvent) onRefreshEvent();
      }
    } catch (err) {
      console.error("Błąd podczas przypisywania do zadania:", err.message);
    }
  };

  const handleUnassignFromTask = async (taskId) => {
    if (!isEventActive)
      return alert("Wyjazd jest zamknięty! Edycja zablokowana.");
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/events/${eventData.id}/tasks/${taskId}/unassign`,
        {
          method: "PUT",
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (response.ok) {
        setFetchedTasks((prev) =>
          prev.map((t) => (t.id === taskId ? { ...t, assignee: null } : t)),
        );
        if (onRefreshEvent) onRefreshEvent();
      } else {
        const errMsg = await response.text();
        alert("Nie możesz odpiąć tego użytkownika! " + errMsg);
      }
    } catch (err) {
      alert("Błąd podczas anulowania przypisania: " + err.message);
    }
  };

  const handleDeleteTaskFromServer = async (taskId) => {
    if (!isEventActive)
      return alert("Wyjazd jest zamknięty! Usuwanie zablokowane.");
    if (!window.confirm("Czy na pewno chcesz usunąć to zadanie?")) return;
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/events/${eventData.id}/tasks/${taskId}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (response.ok) {
        setFetchedTasks((prev) => prev.filter((t) => t.id !== taskId));
        if (onRefreshEvent) onRefreshEvent();
      } else {
        const errMsg = await response.text();
        alert("Nie masz uprawnień do usunięcia tego zadania! " + errMsg);
      }
    } catch (err) {
      alert("Błąd podczas usuwania zadania: " + err.message);
    }
  };

  const handleSaveTask = async (e) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;
    try {
      const savedTaskFromBackend = await createTaskOnServer(
        eventData.id,
        newTaskTitle,
      );
      setFetchedTasks((prev) => [...prev, savedTaskFromBackend]);
      setNewTitle("");
      setIsAddingTask(false);
      if (onRefreshEvent) onRefreshEvent();
    } catch (err) {
      alert("Nie udało się zapisać zadania: " + err.message);
    }
  };

  const handleValueChange = (userId, value) => {
    setExpenseValues((prev) => ({ ...prev, [userId]: value }));
  };

  const handleSaveExpense = async (e) => {
    e.preventDefault();
    if (!expAmount || !expTitle) return alert("Wpisz kwotę i tytuł!");
    if (selectedUsers.length === 0)
      return alert("Zaznacz przynajmniej jedną osobę!");

    const totalExpenseNum = parseFloat(expAmount) || 0;

    if (splitMode === "percent") {
      let percentSum = 0;
      for (const userId of selectedUsers) {
        percentSum += parseFloat(expenseValues[userId]) || 0;
      }
      if (percentSum !== 100) {
        return alert(
          `Suma procentów musi wynosić dokładnie 100%! Aktualnie jest: ${percentSum}%`,
        );
      }
    }

    if (splitMode === "amount") {
      let amountSum = 0;
      for (const userId of selectedUsers) {
        amountSum += parseFloat(expenseValues[userId]) || 0;
      }
      if (amountSum.toFixed(2) !== totalExpenseNum.toFixed(2)) {
        return alert(
          `Suma kwot uczestników (${amountSum.toFixed(2)} zł) musi być równa całkowitej kwocie wydatku (${totalExpenseNum.toFixed(2)} zł)!`,
        );
      }
    }

    const customWeights = {};
    if (splitMode !== "equal") {
      for (const userId of selectedUsers) {
        const val = parseFloat(expenseValues[userId]);
        if (isNaN(val) || val <= 0)
          return alert("Wpisz poprawne wartości dla zaznaczonych osób!");
        customWeights[userId] = val;
      }
    }

    try {
      await createExpenseOnServer(eventData.id, {
        title: expTitle.trim(),
        amount: expAmount,
        splitType: splitMode.toUpperCase(),
        payerId: currentUser?.id,
        participantIds: selectedUsers,
        weights: customWeights,
      });

      setExpAmount("");
      setExpTitle("");
      setExpenseValues({});
      setSplitMode("equal");
      setIsAddingExpense(false);

      await loadExpensesReport();
      if (onRefreshEvent) onRefreshEvent();
    } catch (err) {
      alert("Nie udało się zapisac wydatku: " + err.message);
    }
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
      <div className="hero-counter shadow-sm bg-white p-5 rounded-4 mb-4 border border-light">
        <div className="row align-items-center">
          <div className="col-lg-7 text-start">
            <div className="d-flex align-items-center gap-2 mb-2">
              {isEventActive ? (
                <span className="badge bg-success-light text-success px-3 py-2 rounded-pill fw-bold text-uppercase small">
                  <Activity size={14} className="me-1" />{" "}
                  {eventData.type || "Wydarzenie"}
                </span>
              ) : (
                <span className="badge bg-secondary text-white px-3 py-2 rounded-pill fw-bold text-uppercase small d-flex align-items-center gap-1">
                  <Lock size={12} /> Rozliczone i Zamknięte
                </span>
              )}
            </div>

            <h1 className="fw-bold text-dark display-5 mb-3">
              {eventData.title}
            </h1>
            <div className="d-flex flex-wrap gap-4 text-muted small fw-medium">
              <div className="d-flex align-items-center gap-2">
                <MapPin size={18} className="text-emerald" />
                <span>{eventData.location || "Zakopane, Polska"}</span>
              </div>
              <div className="d-flex align-items-center gap-2">
                <CalendarDays size={18} className="text-emerald" />
                <span>{formattedDate}</span>
              </div>
            </div>
          </div>

          <div className="col-lg-5 mt-4 mt-lg-0">
            <div className="bg-light p-4 rounded-4 d-flex justify-content-around text-center border">
              <div>
                <div className="fs-3 fw-bold text-emerald">
                  {totalExpensesSum.toLocaleString()} zł
                </div>
                <small
                  className="text-muted fw-bold text-uppercase"
                  style={{ fontSize: "10px" }}
                >
                  Suma wydatków
                </small>
              </div>
              <div className="border-start mx-2"></div>
              <div>
                <div className="fs-3 fw-bold text-dark">{totalTasksCount}</div>
                <small
                  className="text-muted fw-bold text-uppercase"
                  style={{ fontSize: "10px" }}
                >
                  Zadania
                </small>
              </div>
              <div className="border-start mx-2"></div>
              <div>
                <div className="fs-3 fw-bold text-dark">{totalPeopleCount}</div>
                <small
                  className="text-muted fw-bold text-uppercase"
                  style={{ fontSize: "10px" }}
                >
                  Uczestnicy
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-4 mb-4">
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
              {fetchedExpenses && fetchedExpenses.length > 0 ? (
                fetchedExpenses.map((exp) => {
                  const payerUser = exp.payer;
                  const displayName = payerUser
                    ? payerUser.name || payerUser.email
                    : "Uczestnik";
                  const displayTitle = exp.description || "Wydatek";
                  const avatarLetter = displayName.charAt(0).toUpperCase();
                  const hasAvatarImage =
                    payerUser?.avatar && payerUser.avatar.startsWith("http");

                  return (
                    <div
                      key={exp.id}
                      className="d-flex justify-content-between align-items-center py-2 border-bottom gap-2"
                    >
                      <div
                        className="d-flex align-items-center gap-2 text-truncate"
                        style={{ flex: 1 }}
                      >
                        {hasAvatarImage ? (
                          <img
                            src={payerUser.avatar}
                            alt={displayName}
                            className="rounded-circle border"
                            style={{
                              width: "24px",
                              height: "24px",
                              objectFit: "cover",
                            }}
                          />
                        ) : (
                          <div
                            className="rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold small shadow-sm"
                            style={{
                              width: "24px",
                              height: "24px",
                              fontSize: "11px",
                              minWidth: "24px",
                            }}
                          >
                            {avatarLetter}
                          </div>
                        )}
                        <span className="text-muted text-truncate small">
                          <strong className="text-dark">{displayName}</strong>:{" "}
                          {displayTitle}
                        </span>
                      </div>
                      <span
                        className="text-success fw-semibold small"
                        style={{ minWidth: "fit-content" }}
                      >
                        {exp.amount} zł
                      </span>
                    </div>
                  );
                })
              ) : (
                <p className="text-muted small text-center my-4">
                  Brak wydatków
                </p>
              )}
            </div>

            <div className="mt-3 pt-3 border-top d-flex flex-column gap-2">
              {/* STREFA FORMULARZA KASY: Uzależniona od twardej flagi isEventActive */}
              {isEventActive ? (
                /* --- WARUNEK 1: WYDARZENIE AKTYWNE --- */
                !isAddingExpense ? (
                  <>
                    <button
                      className="btn btn-emerald w-100 py-2 fw-bold"
                      onClick={() => setIsAddingExpense(true)}
                    >
                      + Dodaj wydatek
                    </button>
                    <button
                      className={`btn w-100 py-2 fw-bold small ${showSettlements ? "btn-dark" : "btn-light text-muted"}`}
                      onClick={() => setShowSettlements(!showSettlements)}
                      style={{ fontSize: "13px" }}
                    >
                      {showSettlements
                        ? "Ukryj rozliczenie"
                        : "📊 Pokaż bilans i rozliczenie"}
                    </button>
                  </>
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
                      {displayAttendees.map((p) => {
                        const isSelected = selectedUsers.includes(p.userId);
                        return (
                          <div
                            key={p.userId}
                            className="d-flex align-items-center justify-content-between py-1 border-bottom-dashed"
                          >
                            <div
                              className="d-flex align-items-center cursor-pointer"
                              style={{ flex: 1 }}
                              onClick={() => toggleUserSelection(p.userId)}
                            >
                              <input
                                type="checkbox"
                                checked={isSelected}
                                readOnly
                                className="custom-checkbox-sm"
                              />
                              <span className="small ms-2">
                                {p.name || "Nieznany"}
                              </span>
                            </div>
                            {isSelected && splitMode !== "equal" && (
                              <div
                                className="d-flex align-items-center"
                                style={{ width: "85px" }}
                              >
                                <input
                                  type="number"
                                  className="form-control form-control-sm text-end"
                                  placeholder="0"
                                  value={expenseValues[p.userId] || ""}
                                  onChange={(e) =>
                                    handleValueChange(p.userId, e.target.value)
                                  }
                                  style={{
                                    padding: "2px 5px",
                                    fontSize: "12px",
                                    borderRadius: "4px",
                                  }}
                                  required
                                />
                                <span
                                  className="text-muted small ms-1"
                                  style={{ fontSize: "11px", minWidth: "15px" }}
                                >
                                  {splitMode === "percent" ? "%" : "zł"}
                                </span>
                              </div>
                            )}
                          </div>
                        );
                      })}
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
                )
              ) : (
                /* --- WARUNEK 2: WYDARZENIE ROZLICZONE / ZAMKNIĘTE --- */
                <>
                  <div className="alert alert-secondary text-center py-3 rounded-3 mb-2 d-flex align-items-center justify-content-center gap-2 small fw-semibold text-muted">
                    <Lock size={16} /> Wydarzenie zostało zamknięte i
                    rozliczone.
                  </div>
                  <button
                    className={`btn w-100 py-2 fw-bold small ${showSettlements ? "btn-dark" : "btn-emerald"}`}
                    onClick={() => setShowSettlements(!showSettlements)}
                  >
                    {showSettlements
                      ? "Ukryj raport rozliczeń"
                      : "📊 Zobacz końcowe bilanse wyjazdu"}
                  </button>
                </>
              )}

              {/* PANEL ORGANIZATORA */}
              {isOrganizer && (
                <div className="bg-light p-3 rounded-3 mt-2 border border-dashed d-flex flex-column gap-2 text-start">
                  <span
                    className="text-muted fw-bold text-uppercase"
                    style={{ fontSize: "9px", letterSpacing: "0.5px" }}
                  >
                    🛠️ Panel Zarządzania Organizatora
                  </span>

                  {isEventActive ? (
                    <button
                      className="btn btn-sm btn-outline-danger w-100 py-2 fw-bold d-flex align-items-center justify-content-center gap-1"
                      onClick={handleCloseEvent}
                    >
                      🔒 Zamknij i zablokuj koszty
                    </button>
                  ) : (
                    <button
                      className="btn btn-sm btn-dark w-100 py-2 fw-bold d-flex align-items-center justify-content-center gap-1"
                      onClick={(e) => {
                        e.preventDefault();
                        const token = localStorage.getItem("token");
                        fetch(
                          `http://localhost:8080/api/reports/${eventData.id}/pdf`,
                          {
                            headers: { Authorization: `Bearer ${token}` },
                          },
                        )
                          .then((res) => {
                            if (!res.ok)
                              throw new Error(
                                "Błąd generowania dokumentu PDF na serwerze",
                              );
                            return res.blob();
                          })
                          .then((blob) => {
                            const url = window.URL.createObjectURL(blob);
                            const a = document.createElement("a");
                            a.href = url;
                            a.download = `Rozliczenie-${eventData.title || "Wyjazdu"}.pdf`;
                            document.body.appendChild(a);
                            a.click();
                            a.remove();
                          })
                          .catch((err) => alert(err.message));
                      }}
                    >
                      <FileText size={14} /> Pobierz oficjalny raport PDF
                    </button>
                  )}
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
              {fetchedTasks &&
                fetchedTasks.map((task) => {
                  const isTaskDone = task.isCompleted || task.isDone;
                  const hasAssignee = !!task.assignee;
                  let displayLetter = "?";

                  if (task.assignee && task.assignee.name) {
                    displayLetter = task.assignee.name.charAt(0).toUpperCase();
                  }

                  return (
                    <div
                      key={task.id}
                      className="task-row d-flex justify-content-between align-items-center py-2 border-bottom gap-2"
                    >
                      <div
                        className="d-flex align-items-center gap-2 text-truncate"
                        style={{ flex: 1 }}
                      >
                        {hasAssignee ? (
                          <div
                            className="avatar-sm-circle bg-info text-white cursor-pointer shadow-sm fw-bold d-flex align-items-center justify-content-center"
                            style={{
                              minWidth: "24px",
                              width: "24px",
                              height: "24px",
                              fontSize: "11px",
                            }}
                            onClick={() => handleUnassignFromTask(task.id)}
                          >
                            {displayLetter}
                          </div>
                        ) : (
                          <div
                            className="avatar-sm-circle bg-light text-success cursor-pointer shadow-sm fw-bold d-flex align-items-center justify-content-center border"
                            style={{
                              minWidth: "24px",
                              width: "24px",
                              height: "24px",
                              fontSize: "14px",
                            }}
                            onClick={() => handleAssignMeToTask(task.id)}
                          >
                            +
                          </div>
                        )}
                        <span
                          className={`task-label text-truncate cursor-pointer ${isTaskDone ? "task-done" : ""}`}
                          onClick={() => handleToggleTaskComplete(task.id)}
                        >
                          {task.description || task.title}
                        </span>
                      </div>
                      {isEventActive && (
                        <div className="d-flex align-items-center">
                          <button
                            className="btn p-0 text-muted hover-danger d-flex align-items-center justify-content-center"
                            style={{ background: "none", border: "none" }}
                            onClick={() => handleDeleteTaskFromServer(task.id)}
                          >
                            <X size={16} />
                          </button>
                        </div>
                      )}
                    </div>
                  );
                })}
            </div>

            {isEventActive && (
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
                      onChange={(e) => setNewTitle(e.target.value)}
                      autoFocus
                      required
                    />
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
            )}
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
              {displayAttendees.map((person) => (
                <div
                  key={person.userId}
                  className="d-flex align-items-center justify-content-between mb-3"
                >
                  <div className="d-flex align-items-center">
                    <div className="avatar-sm-circle bg-success text-white me-2">
                      {person.name ? person.name.charAt(0).toUpperCase() : "U"}
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
            {isEventActive && eventData.joinCode && (
              <div className="mt-3 pt-3 border-top text-center animate-fade-in">
                <div
                  className="p-3 rounded-3 border"
                  style={{ backgroundColor: "#f8f9fa" }}
                >
                  <span
                    className="text-muted fw-bold text-uppercase d-block mb-1"
                    style={{ fontSize: "10px", letterSpacing: "0.5px" }}
                  >
                    Kod zaproszenia
                  </span>
                  <div
                    className="fw-bold text-dark text-break px-2 mb-3"
                    style={{ fontSize: "13px", fontFamily: "monospace" }}
                  >
                    {eventData.joinCode}
                  </div>
                  <button
                    className="btn btn-light btn-sm w-100 py-2 fw-bold text-muted border-0 shadow-sm"
                    style={{ backgroundColor: "#ffffff" }}
                    onClick={handleCopyLink}
                  >
                    Kopiuj kod
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* RAPORT ROZLICZEŃ */}
      {showSettlements && settlementReport && (
        <div className="row g-4 mb-5 animate-fade-in">
          <div className="col-12">
            <div className="card p-4 shadow-sm border-0 rounded-4 bg-white">
              <h4 className="fw-bold text-dark mb-4 d-flex align-items-center gap-2">
                <span>📊 Rozliczenie Wyjazdu:</span>{" "}
                <span className="text-emerald">{eventData.title}</span>
              </h4>
              <div className="row g-4">
                <div className="col-md-6 border-end">
                  <h6
                    className="text-muted fw-bold text-uppercase mb-3"
                    style={{ fontSize: "11px" }}
                  >
                    Stan Konta Uczestników
                  </h6>
                  <div className="d-flex flex-column gap-3">
                    {settlementReport.balancesPerUser &&
                      Object.entries(settlementReport.balancesPerUser).map(
                        ([userName, balance]) => {
                          const isPositive = balance >= 0;
                          return (
                            <div
                              key={userName}
                              className="d-flex justify-content-between align-items-center p-3 rounded-3 bg-light"
                            >
                              <div className="d-flex align-items-center gap-2">
                                <div
                                  className="rounded-circle text-white fw-bold d-flex align-items-center justify-content-center"
                                  style={{
                                    width: "28px",
                                    height: "28px",
                                    fontSize: "12px",
                                    backgroundColor: isPositive
                                      ? "#10b981"
                                      : "#ef4444",
                                  }}
                                >
                                  {userName.charAt(0).toUpperCase()}
                                </div>
                                <span className="fw-medium text-dark small">
                                  {userName}
                                </span>
                              </div>
                              <span
                                className={`fw-bold small ${isPositive ? "text-success" : "text-danger"}`}
                              >
                                {isPositive
                                  ? `+${balance.toFixed(2)}`
                                  : `${balance.toFixed(2)}`}{" "}
                                zł
                              </span>
                            </div>
                          );
                        },
                      )}
                  </div>
                </div>
                <div className="col-md-6">
                  <h6
                    className="text-muted fw-bold text-uppercase mb-3"
                    style={{ fontSize: "11px" }}
                  >
                    Sugerowane Przelewy Zwrotne
                  </h6>
                  <div
                    className="d-flex flex-column gap-2"
                    style={{ maxHeight: "300px", overflowY: "auto" }}
                  >
                    {settlementReport.settlementTransactions &&
                    settlementReport.settlementTransactions.length > 0 ? (
                      settlementReport.settlementTransactions.map((tx, idx) => {
                        const senderName =
                          tx.from?.name || tx.from?.email || "Uczestnik";
                        const receiverName =
                          tx.to?.name || tx.to?.email || "Uczestnik";
                        const displayAmount = Number(tx.amount).toFixed(2);

                        return (
                          <div
                            key={idx}
                            className="d-flex justify-content-between align-items-center p-3 border rounded-3 border-light bg-white shadow-2xs"
                          >
                            <div
                              className="d-flex align-items-center gap-2 text-truncate"
                              style={{ flex: 1 }}
                            >
                              <span
                                className="fw-bold text-danger small text-truncate"
                                style={{ maxWidth: "120px" }}
                                title={senderName}
                              >
                                {senderName}
                              </span>
                              <ArrowRight
                                size={14}
                                className="text-muted flex-shrink-0"
                              />
                              <span
                                className="fw-bold text-success small text-truncate"
                                style={{ maxWidth: "120px" }}
                                title={receiverName}
                              >
                                {receiverName}
                              </span>
                            </div>
                            <span
                              className="fw-bold text-dark text-nowrap ms-2"
                              style={{ fontSize: "14px" }}
                            >
                              {displayAmount} zł
                            </span>
                          </div>
                        );
                      })
                    ) : (
                      <div className="text-center py-4 text-muted small">
                        🎉 Wszyscy są rozliczeni na zero! Brak wymaganych
                        zwrotów.
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
