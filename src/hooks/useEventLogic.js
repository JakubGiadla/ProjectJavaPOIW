import { useState } from "react";
import { useEventApi } from "./useEventApi";

export const useEventLogic = (dbEvents) => {
  const [activeEventId, setActiveEventId] = useState(null);
  const [detailedEvent, setDetailedEvent] = useState(null);
  const { fetchEventDetails } = useEventApi();

  // Funkcja wywoływana przy wyborze kafelka z listy wyjazdów
  const handleSelectEvent = async (id) => {
    setActiveEventId(id);
    if (!id) {
      setDetailedEvent(null);
      return;
    }
    // Pobieramy pełne EventDetailsDTO ze Spring Boota
    const details = await fetchEventDetails(id);
    if (details) {
      setDetailedEvent(details);
    }
  };

  // Znajduje wybrane wydarzenie na podstawie dociągniętych szczegółów lub listy ogólnej
  const activeEvent =
    detailedEvent || dbEvents.find((e) => e.id === activeEventId);

  // LOGIKA ZADAŃ
  const handleTaskAction = async (taskId, emailToAssign = null) => {
    if (!detailedEvent) return;

    if (emailToAssign) {
      // Przypisywanie osoby po emailu
      setDetailedEvent((prev) => ({
        ...prev,
        tasks: (prev.tasks || []).map((t) =>
          t.id === taskId
            ? {
                ...t,
                assignee: {
                  name: emailToAssign.split("@")[0],
                  email: emailToAssign,
                },
              }
            : t,
        ),
      }));
    } else {
      // Przełączanie statusu ukończenia (isCompleted)
      setDetailedEvent((prev) => ({
        ...prev,
        tasks: (prev.tasks || []).map((t) =>
          t.id === taskId ? { ...t, isCompleted: !t.isCompleted } : t,
        ),
      }));
    }
  };

  const addTask = (title) => {
    if (!detailedEvent) return;
    const newTask = {
      id: Date.now(),
      description: title,
      isCompleted: false,
      assignee: null,
    };
    setDetailedEvent((prev) => ({
      ...prev,
      tasks: [...(prev.tasks || []), newTask],
    }));
  };

  return {
    activeEvent,
    setActiveEventId: handleSelectEvent,
    handleTaskAction,
    addTask,
  };
};
