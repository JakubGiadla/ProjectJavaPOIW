import React, { useState } from "react";
import { Smile, User } from "lucide-react";
import { jwtDecode } from "jwt-decode";

const Login = ({ onLogin }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [isRegistering, setIsRegistering] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setIsLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Błędny e-mail lub hasło");
      }

      const data = await response.json();
      localStorage.setItem("token", data.token);

      const decoded = jwtDecode(data.token);

      // Mapujemy dane użytkownika bezpośrednio z tokenu ze Spring Boota
      const loggedInUser = {
        id: decoded.userId || 101, // Spring powinien wystawiać userId w tokenie
        name: decoded.name || decoded.sub.split("@")[0],
        email: decoded.sub,
        avatar: (decoded.name || decoded.sub).substring(0, 2).toUpperCase(),
        color: "bg-success",
      };

      onLogin(loggedInUser);
    } catch (err) {
      setError(err.message || "Nie udało się połączyć ze Spring Bootem");
    } finally {
      setIsLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setIsLoading(true);

    if (!name || !email || !password) {
      setError("Wszystkie pola są wymagane!");
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, name }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Błąd podczas rejestracji");
      }

      setSuccess(`Konto dla ${name} utworzone! Możesz się teraz zalogować.`);
      setIsRegistering(false);
    } catch (err) {
      setError(err.message || "Błąd połączenia z bazą");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center vh-100 bg-light">
      <div
        className="card shadow-sm border-0 p-5 bg-white text-center"
        style={{ maxWidth: "420px", width: "100%", borderRadius: "32px" }}
      >
        <div
          className="text-start fw-bold fs-4 mb-5 text-dark"
          style={{ letterSpacing: "-0.5px" }}
        >
          Event<span style={{ color: "#10b981" }}>Split</span>
        </div>

        {error && (
          <div className="alert alert-danger small py-2 rounded-3">{error}</div>
        )}
        {success && (
          <div className="alert alert-success small py-2 rounded-3">
            {success}
          </div>
        )}

        {isRegistering ? (
          <form onSubmit={handleRegister}>
            <div className="mb-4 text-center">
              <Smile size={72} color="#10b981" className="mb-4 mx-auto" />
              <h3
                className="fw-bold text-dark mb-2"
                style={{ fontSize: "24px" }}
              >
                Jak się nazywasz?
              </h3>
              <p className="text-muted small px-3">
                Podaj e-mail, hasło oraz imię, aby zacząć korzystać z aplikacji.
              </p>
            </div>

            <div className="mb-3 text-start">
              <input
                type="text"
                className="form-control py-3"
                style={{ borderRadius: "14px" }}
                placeholder="Twoje imię"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="mb-3 text-start">
              <input
                type="email"
                className="form-control py-3"
                style={{ borderRadius: "14px" }}
                placeholder="twoj@email.pl"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="mb-4 text-start">
              <input
                type="password"
                className="form-control py-3"
                style={{ borderRadius: "14px" }}
                placeholder="Hasło"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-emerald w-100 py-3 fw-bold text-white shadow-sm mb-3"
              disabled={isLoading}
            >
              {isLoading ? "Zapisywanie..." : "Zarejestruj się"}
            </button>
            <button
              type="button"
              className="btn btn-link w-100 text-decoration-none text-muted small"
              onClick={() => setIsRegistering(false)}
            >
              Wróć do logowania
            </button>
          </form>
        ) : (
          <form onSubmit={handleLogin}>
            <h3
              className="fw-bold text-dark mb-4 text-start"
              style={{ fontSize: "28px" }}
            >
              Zaloguj się
            </h3>
            <div className="mb-3 text-start">
              <label className="form-label small fw-bold text-muted">
                E-mail
              </label>
              <input
                type="email"
                className="form-control py-3"
                style={{ borderRadius: "14px" }}
                placeholder="twoj@email.pl"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="mb-4 text-start">
              <label className="form-label small fw-bold text-muted">
                Hasło
              </label>
              <input
                type="password"
                className="form-control py-3"
                style={{ borderRadius: "14px" }}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button
              type="submit"
              className="btn btn-emerald w-100 py-3 fw-bold text-white mb-3"
              disabled={isLoading}
            >
              {isLoading ? "Logowanie..." : "Zaloguj się"}
            </button>
            <button
              type="button"
              className="btn btn-outline-light w-100 py-2 text-muted border-0 small"
              onClick={() => setIsRegistering(true)}
            >
              Nie masz konta?{" "}
              <span style={{ color: "#10b981", fontWeight: "bold" }}>
                Zarejestruj się
              </span>
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default Login;
