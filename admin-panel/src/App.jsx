import React, { useEffect, useState } from "react";
import { onAuthStateChanged, signOut } from "firebase/auth";
import { auth } from "./firebase";
import LoginView from "./views/LoginView";
import DashboardView from "./views/DashboardView";
import PromptsView from "./views/PromptsView";
import PricingView from "./views/PricingView";
import UsersView from "./views/UsersView";
import { LayoutDashboard, FileSliders, Settings, Users, LogOut } from "lucide-react";
import "./App.css";

export default function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("dashboard"); // dashboard, prompts, pricing, users

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (firebaseUser) => {
      setUser(firebaseUser);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

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
  if (!user) {
    return <LoginView />;
  }

  return (
    <div className="app-container">
      {/* Sidebar Navigation */}
      <aside className="sidebar">
        <div>
          <div className="sidebar-logo">Prompt Architect</div>
          <ul className="sidebar-menu">
            <li>
              <div 
                className={`menu-item ${activeTab === "dashboard" ? "active" : ""}`}
                onClick={() => setActiveTab("dashboard")}
              >
                <LayoutDashboard size={18} />
                Dashboard
              </div>
            </li>
            <li>
              <div 
                className={`menu-item ${activeTab === "prompts" ? "active" : ""}`}
                onClick={() => setActiveTab("prompts")}
              >
                <FileSliders size={18} />
                System Prompts
              </div>
            </li>
            <li>
              <div 
                className={`menu-item ${activeTab === "pricing" ? "active" : ""}`}
                onClick={() => setActiveTab("pricing")}
              >
                <Settings size={18} />
                Pricing Packages
              </div>
            </li>
            <li>
              <div 
                className={`menu-item ${activeTab === "users" ? "active" : ""}`}
                onClick={() => setActiveTab("users")}
              >
                <Users size={18} />
                Manage Users
              </div>
            </li>
          </ul>
        </div>

        <div className="sidebar-logout">
          <div className="menu-item" onClick={handleLogout} style={{ color: "#ef4444" }}>
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
