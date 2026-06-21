import { useState } from "react";

export const useExpenseApi = () => {
  const [isExpenseLoading, setIsExpenseLoading] = useState(false);
  const [expenseError, setExpenseError] = useState(null);

  const createExpenseOnServer = async (eventId, expenseData) => {
    setIsExpenseLoading(true);
    setExpenseError(null);
    try {
      const token = localStorage.getItem("token");

      const payload = {
        title: expenseData.title,
        amount: Number(expenseData.amount) || 0,
        splitType: expenseData.splitType, // EQUAL, PERCENT lub AMOUNT
        payerId: expenseData.payerId,
        participantIds: expenseData.participantIds,
        weights: expenseData.weights || {}, // Przesyłamy mapę wag przypisanych do ID użytkowników
      };

      console.log("Wysyłam zaawansowany wydatek do Spring Boota:", payload);

      const response = await fetch(
        `http://localhost:8080/api/expenses/${eventId}`,
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
          `Serwer odrzucił wydatek (${response.status}): ${errorText}`,
        );
      }

      return await response.json();
    } catch (err) {
      setExpenseError(err.message);
      throw err;
    } finally {
      setIsExpenseLoading(false);
    }
  };

  return { createExpenseOnServer, isExpenseLoading, expenseError };
};
