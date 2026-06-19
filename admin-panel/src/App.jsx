import React, { useEffect, useState } from "react";
import { onAuthStateChanged, signOut } from "firebase/auth";
import { auth } from "./firebase";
import LoginView from "./views/LoginView";
import DashboardView from "./views/DashboardView";
import PromptsView from "./views/PromptsView";
import PricingView from "./views/PricingView";
import UsersView from "./views/UsersView";
import { LayoutDashboard, FileSliders, Settings, Users, LogOut, Sun, Moon, Menu, X } from "lucide-react";
import "./App.css";

export default function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("dashboard"); // dashboard, prompts, pricing, users
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "dark");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const devBypass = true; // Set to true to view layouts without Firebase Auth credentials locally

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (firebaseUser) => {
      setUser(firebaseUser);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  useEffect(() => {
    if (theme === "light") {
      document.body.classList.add("light-mode");
    } else {
      document.body.classList.remove("light-mode");
    }
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => (prev === "dark" ? "light" : "dark"));
  };

  const handleLogout = () => {
    signOut(auth);
  };

  if (loading) {
    return (
      <div style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "100vh",
        backgroundColor: "#020617",
        color: "#f8fafc"
      }}>
        <h2>Initializing Prompt Architect...</h2>
      </div>
    );
  }

  // Auth gate
  if (!user && !devBypass) {
    return <LoginView />;
  }

  return (
    <div className="app-container">
      {/* Mobile Navbar */}
      <header className="mobile-navbar">
        <button className="menu-toggle-btn" onClick={() => setSidebarOpen(true)}>
          <Menu size={24} />
        </button>
        <div className="mobile-logo-container">
          <img src="/logo.png" alt="Logo" className="mobile-logo-img" />
          <span className="mobile-logo-text">Prompt Architect</span>
        </div>
      </header>

      {/* Sidebar Backdrop Overlay for Mobile */}
      {sidebarOpen && (
        <div className="sidebar-backdrop" onClick={() => setSidebarOpen(false)}></div>
      )}

      {/* Sidebar Navigation */}
      <aside className={`sidebar ${sidebarOpen ? "sidebar-open" : ""}`}>
        <div>
          <div className="sidebar-header-mobile">
            <div className="sidebar-logo-container">
              <img src="/logo.png" alt="Logo" className="sidebar-logo-img" />
              <div className="sidebar-logo">Prompt Architect</div>
            </div>
            <button className="sidebar-close-btn" onClick={() => setSidebarOpen(false)}>
              <X size={20} />
            </button>
          </div>

          <div className="sidebar-logo-container desktop-logo-only">
            <img src="/logo.png" alt="Logo" className="sidebar-logo-img" />
            <div className="sidebar-logo">Prompt Architect</div>
          </div>
          
          {/* Theme Toggle Button */}
          <div className="theme-toggle-btn" onClick={toggleTheme}>
            {theme === "dark" ? (
              <>
                <Sun size={18} style={{ color: "#fbbf24" }} />
                <span>Light Mode</span>
              </>
            ) : (
              <>
                <Moon size={18} style={{ color: "#60a5fa" }} />
                <span>Dark Mode</span>
              </>
            )}
          </div>

          <ul className="sidebar-menu">
            <li>
              <div 
                className={`menu-item ${activeTab === "dashboard" ? "active" : ""}`}
                onClick={() => {
                  setActiveTab("dashboard");
                  setSidebarOpen(false);
                }}
              >
                <LayoutDashboard size={18} />
                Dashboard
              </div>
            </li>
            <li>
              <div 
                className={`menu-item ${activeTab === "prompts" ? "active" : ""}`}
                onClick={() => {
                  setActiveTab("prompts");
                  setSidebarOpen(false);
                }}
              >
                <FileSliders size={18} />
                System Prompts
              </div>
            </li>
            <li>
              <div 
                className={`menu-item ${activeTab === "pricing" ? "active" : ""}`}
                onClick={() => {
                  setActiveTab("pricing");
                  setSidebarOpen(false);
                }}
              >
                <Settings size={18} />
                Pricing Packages
              </div>
            </li>
            <li>
              <div 
                className={`menu-item ${activeTab === "users" ? "active" : ""}`}
                onClick={() => {
                  setActiveTab("users");
                  setSidebarOpen(false);
                }}
              >
                <Users size={18} />
                Manage Users
              </div>
            </li>
          </ul>
        </div>

        <div className="sidebar-logout">
          <div className="menu-item" onClick={() => {
            handleLogout();
            setSidebarOpen(false);
          }} style={{ color: "#ef4444" }}>
            <LogOut size={18} />
            Log Out
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="main-content">
        {activeTab === "dashboard" && <DashboardView />}
        {activeTab === "prompts" && <PromptsView />}
        {activeTab === "pricing" && <PricingView />}
        {activeTab === "users" && <UsersView />}
      </main>
    </div>
  );
}
