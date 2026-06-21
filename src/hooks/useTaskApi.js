import { useState } from "react";

export const useTaskApi = () => {
  const [isTaskLoading, setIsTaskLoading] = useState(false);
  const [taskError, setTaskError] = useState(null);

  const createTaskOnServer = async (eventId, taskTitle) => {
    setIsTaskLoading(true);
    setTaskError(null);
    try {
      const token = localStorage.getItem("token");

      // Struktura idealnie dopasowana pod klasę Task.java na backendzie
      const payload = {
        description: taskTitle.trim(),
        isCompleted: false,
        assignee: null, // Zadanie na start nie ma przypisanej osoby
      };

      console.log(
        "🚀 useTaskApi wysyła POST do Springa pod event ID:",
        eventId,
        payload,
      );

      const response = await fetch(
        `http://localhost:8080/api/events/${eventId}/tasks`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        },
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `Serwer odrzucił zadanie (${response.status}): ${errorText}`,
        );
      }

      return await response.json();
    } catch (err) {
      console.error("Błąd useTaskApi (createTaskOnServer):", err.message);
      setTaskError(err.message);
      throw err;
    } finally {
      // 🔥 TUTAJ: Poprawione z 'verify' na 'finally'
      setIsTaskLoading(false);
    }
  };

  return {
    createTaskOnServer,
    isTaskLoading,
    taskError,
  };
};
