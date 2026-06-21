import React, { useState } from "react";
import { Layers } from "lucide-react";
import ProfileDropdown from "../profile/ProfileDropdown";
import "./Navbar.css";

const Navbar = ({
  user,
  onLogout,
  notifications,
  setNotifications,
  onBack,
}) => {
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const markAllAsRead = () => {
    const updatedNotifications = notifications.map((n) => ({
      ...n,
      isRead: true,
    }));
    setNotifications(updatedNotifications);
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  // Wyciągamy pierwszą literę lub dwie pierwsze litery imienia do małego kółka
  const userInitials = user?.name
    ? user.name.substring(0, 2).toUpperCase()
    : "U";
  const firstName = user?.name ? user.name.split(" ")[0] : "Użytkownik";

  return (
    <nav className="navbar navbar-light bg-white border-bottom py-3 mb-4 shadow-sm position-relative z-3">
      <div className="container">
        <div className="navbar-brand fw-bold text-emerald d-flex align-items-center">
          <Layers className="me-2" /> EventSplit
        </div>

        {/* DYNAMICZNY AVATAR I IMIĘ */}
        <div
          className="d-flex align-items-center gap-2 border rounded-pill ps-2 pe-3 py-1 cursor-pointer hover-bg-light transition-all shadow-sm position-relative"
          onClick={() => setIsProfileOpen(!isProfileOpen)}
        >
          <div className="avatar-sm-circle bg-success-light text-success fw-bold">
            {userInitials}
          </div>
          <span className="small fw-bold">{firstName}</span>

          {unreadCount > 0 && (
            <span
              className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
              style={{ fontSize: "0.6rem" }}
            >
              {unreadCount}
            </span>
          )}
        </div>
      </div>

      {isProfileOpen && (
        <ProfileDropdown
          user={user} // Przekazujemy dalej do menu opcji
          onLogout={onLogout}
          notifications={notifications}
          onMarkRead={markAllAsRead}
          onClose={() => setIsProfileOpen(false)}
          onBack={onBack}
        />
      )}
    </nav>
  );
};

export default Navbar;
