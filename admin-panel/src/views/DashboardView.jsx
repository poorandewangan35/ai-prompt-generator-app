import React, { useEffect, useState } from "react";
import { collection, onSnapshot, query, orderBy, limit } from "firebase/firestore";
import { db } from "../firebase";
import { Users, FileText, ShoppingCart, Activity, ShieldCheck, ShieldAlert } from "lucide-react";

export default function DashboardView() {
  const [userCount, setUserCount] = useState(0);
  const [promptCount, setPromptCount] = useState(0);
  const [receiptCount, setReceiptCount] = useState(0);
  const [recentActivities, setRecentActivities] = useState([]);
  const [apiKeyConfigured, setApiKeyConfigured] = useState(null);

  useEffect(() => {
    // Realtime listeners for quick aggregation counts
    const unsubscribeUsers = onSnapshot(collection(db, "users"), (snapshot) => {
      setUserCount(snapshot.size);
    });

    const unsubscribePrompts = onSnapshot(collection(db, "prompts"), (snapshot) => {
      setPromptCount(snapshot.size);
    });

    const unsubscribeReceipts = onSnapshot(collection(db, "receipts"), (snapshot) => {
      setReceiptCount(snapshot.size);
    });

    // Check Gemini API key configuration status
    const unsubscribeConfig = onSnapshot(collection(db, "config"), (snapshot) => {
      const systemDoc = snapshot.docs.find(d => d.id === "system");
      if (systemDoc && systemDoc.exists()) {
        const key = systemDoc.data().geminiApiKey;
        setApiKeyConfigured(key && key.startsWith("AIzaSy") && key.length > 15);
      } else {
        setApiKeyConfigured(false);
      }
    });

    // Fetch the 5 most recent generation logs
    const recentQuery = query(collection(db, "prompts"), orderBy("createdAt", "desc"), limit(5));
    const unsubscribeRecent = onSnapshot(recentQuery, (snapshot) => {
      const logs = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setRecentActivities(logs);
    });

    return () => {
      unsubscribeUsers();
      unsubscribePrompts();
      unsubscribeReceipts();
      unsubscribeConfig();
      unsubscribeRecent();
    };
  }, []);

  return (
    <div>
      <div className="view-header">
        <h1>Dashboard Analytics</h1>
        <p>Real-time analytics and generation stream for Prompt Architect</p>
      </div>

      {/* Metric cards grid */}
      <div className="metrics-grid">
        <div className="metric-card">
          <div className="metric-info">
            <h3>Registered Users</h3>
            <div className="value">{userCount}</div>
          </div>
          <div className="metric-icon" style={{ backgroundColor: "rgba(37, 99, 235, 0.15)", color: "#3b82f6" }}>
            <Users size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-info">
            <h3>Prompts Generated</h3>
            <div className="value">{promptCount}</div>
          </div>
          <div className="metric-icon" style={{ backgroundColor: "rgba(13, 148, 136, 0.15)", color: "#0d9488" }}>
            <FileText size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-info">
            <h3>Paid Receipts</h3>
            <div className="value">{receiptCount}</div>
          </div>
          <div className="metric-icon" style={{ backgroundColor: "rgba(34, 211, 238, 0.15)", color: "#22d3ee" }}>
            <ShoppingCart size={24} />
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-info">
            <h3>Gemini API Status</h3>
            <div className="value" style={{ fontSize: "16px", marginTop: "8px", fontWeight: "600" }}>
              {apiKeyConfigured === null ? (
                "Checking..."
              ) : apiKeyConfigured ? (
                <span style={{ color: "#2dd4bf", display: "flex", alignItems: "center", gap: "6px" }}>
                  <ShieldCheck size={18} /> Active
                </span>
              ) : (
                <span style={{ color: "#ef4444", display: "flex", alignItems: "center", gap: "6px" }}>
                  <ShieldAlert size={18} /> Configuration Required
                </span>
              )}
            </div>
          </div>
          <div className="metric-icon" style={{ backgroundColor: apiKeyConfigured ? "rgba(45, 212, 191, 0.15)" : "rgba(239, 68, 68, 0.15)", color: apiKeyConfigured ? "#2d4" : "#f44" }}>
            <Activity size={24} />
          </div>
        </div>
      </div>

      {/* Live prompt generation logs */}
      <div className="glass-card">
        <h2 className="card-title">Live Generation Feed</h2>
        {recentActivities.length === 0 ? (
          <p style={{ color: "var(--text-secondary)", fontSize: "14px", textAlign: "center", padding: "20px" }}>
            No generation events recorded yet.
          </p>
        ) : (
          <div className="table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Project Name</th>
                  <th>Type</th>
                  <th>Core Requirement Idea</th>
                  <th>Date Created</th>
                </tr>
              </thead>
              <tbody>
                {recentActivities.map((log) => (
                  <tr key={log.id}>
                    <td style={{ fontWeight: "600" }}>{log.name || "Unnamed Project"}</td>
                    <td>
                      <span
                        style={{
                          padding: "4px 8px",
                          borderRadius: "6px",
                          fontSize: "11px",
                          fontWeight: "700",
                          backgroundColor: log.type === "App" ? "rgba(13, 148, 136, 0.15)" : "rgba(37, 99, 235, 0.15)",
                          color: log.type === "App" ? "#2dd4bf" : "#60a5fa"
                        }}
                      >
                        {log.type.toUpperCase()}
                      </span>
                    </td>
                    <td style={{ color: "var(--text-secondary)", maxWidth: "300px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                      {log.idea}
                    </td>
                    <td>{new Date(log.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
