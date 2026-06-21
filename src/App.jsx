import { useState, useEffect } from "react";
import Navbar from "./components/layout/Navbar";
import Dashboard from "./features/dashboard/Dashboard";
import Login from "./features/auth/Login";
import WelcomeScreen from "./features/auth/WelcomeScreen";
import EventSelector from "./features/dashboard/EventSelector";
import CreateEventModal from "./features/dashboard/CreateEventModal";

import { useEventLogic } from "./hooks/useEventLogic";
import { useEventApi } from "./hooks/useEventApi";
import { initialNotifications } from "./components/layout/mockNotifications";

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [notifications, setNotifications] = useState(initialNotifications);
  const [dbEvents, setDbEvents] = useState([]);

  const { fetchEvents, isLoading: isApiLoading } = useEventApi();
  const { activeEvent, setActiveEventId, handleTaskAction, addTask } =
    useEventLogic(dbEvents);

  useEffect(() => {
    if (!currentUser) return;

    const loadUserEvents = async () => {
      const data = await fetchEvents();
      setDbEvents(data);
    };

    loadUserEvents();
  }, [currentUser]);

  const handleCreateEventAndRefresh = async (eventData) => {
    try {
      const token = localStorage.getItem("token");

      const payload = {
        title: eventData.title,
        date: eventData.date ? `${eventData.date}T00:00:00` : null,
        location: eventData.location,
        type: eventData.type,
        maxAttendees: eventData.maxAttendees,
      };

      console.log("🔥 Tworzenie wydarzenia - payload dla Springa:", payload);

      const response = await fetch("http://localhost:8080/api/events", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(`Serwer zwrócił błąd statusu: ${response.status}`);
      }

      const newEvent = await response.json();
      console.log("✅ Sukces! Wydarzenie zapisane:", newEvent);

      const freshList = await fetchEvents();
      setDbEvents(freshList);
      setActiveEventId(newEvent.id);
    } catch (err) {
      console.error("❌ Błąd podczas tworzenia:", err.message);
      alert("Nie udało się utworzyć wydarzenia: " + err.message);
    }
  };

  const refreshEvents = async () => {
    const freshList = await fetchEvents();
    setDbEvents(freshList);
  };

  const renderContent = () => {
    if (!currentUser) return <Login onLogin={(user) => setCurrentUser(user)} />;

    if (isApiLoading) {
      return (
        <div className="text-center p-5 text-muted">
          Komunikacja ze Spring Boot (Docker)...
        </div>
      );
    }

    if (!activeEvent) {
      if (dbEvents.length > 0) {
        return (
          <EventSelector
            events={dbEvents}
            onSelectEvent={setActiveEventId}
            onCreateNew={() => setIsCreateModalOpen(true)}
            onRefreshList={refreshEvents}
          />
        );
      }
      return (
        <WelcomeScreen
          onCreateEvent={() => setIsCreateModalOpen(true)}
          onRefreshAllEvents={async () => {
            // Twardo dociągamy świeżą listę wydarzeń dla usera po wpisaniu kodu
            const freshList = await fetchEvents();
            setDbEvents(freshList);
          }}
        />
      );
    }

    return (
      <Dashboard
        eventData={activeEvent}
        onTaskAction={handleTaskAction}
        onBack={() => setActiveEventId(null)}
        onAddTask={addTask}
        currentUser={currentUser}
        onRefreshEvent={() => setActiveEventId(activeEvent.id)}
      />
    );
  };

  return (
    <div className="min-vh-100 d-flex flex-column">
      {currentUser && (
        <Navbar
          user={currentUser}
          onLogout={() => {
            localStorage.removeItem("token");
            setCurrentUser(null);
            setDbEvents([]);
            setActiveEventId(null);
          }}
          notifications={notifications}
          setNotifications={setNotifications}
          onBack={() => setActiveEventId(null)}
        />
      )}
      <main className="flex-grow-1">{renderContent()}</main>

      <CreateEventModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onCreate={handleCreateEventAndRefresh}
      />
    </div>
  );
}

export default App;
