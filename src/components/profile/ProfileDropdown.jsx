import React, { useState, useEffect, useRef } from "react";
import {
  Bell,
  Lock,
  ChevronRight,
  Calendar,
  Wallet,
  LogOut,
  ArrowLeft,
  Check,
  Moon,
  CreditCard,
  ListChecks,
  UserPlus,
  Grid,
} from "lucide-react";

const ProfileDropdown = ({
  user,
  onLogout,
  notifications,
  onMarkRead,
  onClose,
  onBack,
}) => {
  const [view, setView] = useState("main");
  const dropdownRef = useRef(null);

  const iconMap = {
    expense: <CreditCard size={20} />,
    task: <ListChecks size={20} />,
    info: <Moon size={20} />,
    user: <UserPlus size={20} />,
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        onClose();
      }
    };

    const handleEsc = (event) => {
      if (event.key === "Escape") onClose();
    };

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEsc);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEsc);
    };
  }, [onClose]);

  if (view === "notifications") {
    return (
      <div
        ref={dropdownRef}
        className="profile-dropdown animate-fade-in shadow-lg bg-white rounded-4 position-absolute border overflow-hidden"
        style={{ top: "75px", right: "10%", width: "350px", zIndex: 1000 }}
      >
        <div className="d-flex align-items-center justify-content-between p-3 border-bottom bg-white sticky-top">
          <div className="d-flex align-items-center gap-2">
            <ArrowLeft
              className="cursor-pointer"
              size={20}
              onClick={() => setView("main")}
            />
            <h6 className="fw-bold mb-0">Powiadomienia</h6>
          </div>
          <Check
            className="text-emerald cursor-pointer"
            size={20}
            onClick={onMarkRead}
          />
        </div>
        <div className="p-3 overflow-auto" style={{ maxHeight: "450px" }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              className={`p-3 rounded-4 mb-2 position-relative shadow-sm border ${
                n.isRead
                  ? "bg-light-gray border-0"
                  : "bg-white border-emerald-light"
              }`}
            >
              <div className="d-flex gap-3">
                <div
                  className={`icon-box bg-${n.colorClass}-light text-${n.colorClass} rounded-3 p-2`}
                >
                  {iconMap[n.type] || <Bell size={20} />}
                </div>
                <div>
                  <h6 className="small fw-bold mb-1">{n.title}</h6>
                  <p className="text-muted mb-1 small">{n.desc}</p>
                  <small
                    className="text-muted-light"
                    style={{ fontSize: "10px" }}
                  >
                    {n.time}
                  </small>
                </div>
              </div>
              {!n.isRead && (
                <div
                  className="status-dot bg-success position-absolute"
                  style={{
                    top: "15px",
                    right: "15px",
                    width: "8px",
                    height: "8px",
                    borderRadius: "50%",
                  }}
                ></div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  }

  // DYNAMICZNE PARAMETRY DLA ROZWIJANEGO MENU
  const userInitials = user?.name
    ? user.name.substring(0, 2).toUpperCase()
    : "U";
  const fullName = user?.name || "Użytkownik Testowy";
  const userEmail = user?.email || "brak_maila@example.com";

  return (
    <div
      ref={dropdownRef}
      className="profile-dropdown animate-fade-in shadow-lg bg-white rounded-4 p-4 position-absolute border"
      style={{ top: "75px", right: "10%", width: "350px", zIndex: 1000 }}
    >
      <div className="text-center mb-4">
        <div
          className="mx-auto bg-success-light text-success rounded-circle d-flex align-items-center justify-content-center mb-2 fw-bold"
          style={{ width: "60px", height: "60px", fontSize: "1.5rem" }}
        >
          {userInitials} {/* Prawdziwy awatar */}
        </div>
        <h6 className="fw-bold mb-0">{fullName}</h6> {/* Prawdziwe imię */}
        <small className="text-muted">{userEmail}</small>{" "}
        {/* Prawdziwy email */}
      </div>

      <div className="row g-2 mb-4 text-center">
        <div className="col-6">
          <div className="bg-light p-3 rounded-4">
            <Calendar size={18} className="text-emerald mb-2" />
            <div className="fw-bold">0</div>
            <small className="text-muted">Wydarzenia</small>
          </div>
        </div>
        <div className="col-6">
          <div className="bg-light p-3 rounded-4">
            <Wallet size={18} className="text-emerald mb-2" />
            <div className="fw-bold">0 zł</div>
            <small className="text-muted">Wydatki</small>
          </div>
        </div>
      </div>

      <div className="profile-menu-list mb-3">
        <div
          className="d-flex align-items-center justify-content-between py-2 border-bottom cursor-pointer hover-text-emerald"
          onClick={() => {
            onBack();
            onClose();
          }}
        >
          <div className="d-flex align-items-center gap-3 small">
            <Grid size={16} className="text-muted" />{" "}
            <span>Zmień wydarzenie</span>
          </div>
          <ChevronRight size={14} className="text-muted" />
        </div>
        <div
          className="d-flex align-items-center justify-content-between py-2 border-bottom cursor-pointer hover-text-emerald"
          onClick={() => setView("notifications")}
        >
          <div className="d-flex align-items-center gap-3 small">
            <Bell size={16} className="text-muted" /> <span>Powiadomienia</span>
            {notifications.filter((n) => !n.isRead).length > 0 && (
              <span
                className="badge bg-danger rounded-pill ms-1"
                style={{ fontSize: "8px" }}
              >
                {notifications.filter((n) => !n.isRead).length}
              </span>
            )}
          </div>
          <ChevronRight size={14} className="text-muted" />
        </div>
        <div className="d-flex align-items-center justify-content-between py-2 border-bottom cursor-pointer hover-text-emerald">
          <div className="d-flex align-items-center gap-3 small">
            <Lock size={16} className="text-muted" /> <span>Zmień hasło</span>
          </div>
          <ChevronRight size={14} className="text-muted" />
        </div>
        <div className="d-flex align-items-center justify-content-between py-2 border-bottom cursor-pointer hover-text-emerald">
          <div className="d-flex align-items-center gap-3 small">
            <Moon size={16} className="text-muted" /> <span>Motyw</span>
          </div>
          <ChevronRight size={14} className="text-muted" />
        </div>
      </div>

      <button
        className="btn btn-outline-danger w-100 py-2 small fw-bold d-flex align-items-center justify-content-center gap-2"
        onClick={onLogout}
      >
        <LogOut size={16} /> Wyloguj się
      </button>
    </div>
  );
};

export default ProfileDropdown;
