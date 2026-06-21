import { useState } from "react";

export const useEventApi = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchEvents = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/api/events", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });
      if (!response.ok) throw new Error(`Status: ${response.status}`);
      return await response.json();
    } catch (err) {
      console.error(err.message);
      setError(err.message);
      return [];
    } finally {
      setIsLoading(false);
    }
  };

  const fetchEventDetails = async (eventId) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(
        `http://localhost:8080/api/events/${eventId}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        },
      );
      if (!response.ok) throw new Error(`Status: ${response.status}`);
      return await response.json();
    } catch (err) {
      console.error(err.message);
      return null;
    }
  };

  const createEventOnServer = async (eventData) => {
    setIsLoading(true);
    setError(null);
    try {
      const token = localStorage.getItem("token");

      // WYSYŁAMY TYLKO TO, CO ISTNIEJE W ENTITY EVENT.JAVA NA BACKENDZIE!
      const payload = {
        title: eventData.title,
        date: eventData.date ? `${eventData.date}T00:00:00` : null, // Konwersja na LocalDateTime (Format ISO)
        location: eventData.location || "Polska",
        type: eventData.type || "Wyjazd",
        maxAttendees: Number(eventData.maxAttendees) || 10,
      };

      const response = await fetch("http://localhost:8080/api/events", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error(`Status: ${response.status}`);
      return await response.json();
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    fetchEvents,
    fetchEventDetails,
    createEventOnServer,
    isLoading,
    error,
  };
};
