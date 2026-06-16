import React, { useEffect, useState } from "react";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { db } from "../firebase";

export default function PromptsView() {
  const [websitePrompt, setWebsitePrompt] = useState("");
  const [appPrompt, setAppPrompt] = useState("");
  const [apiKey, setApiKey] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const docRef = doc(db, "config", "system");
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          const data = docSnap.data();
          setWebsitePrompt(data.websiteSystemPrompt || "");
          setAppPrompt(data.appSystemPrompt || "");
          setApiKey(data.geminiApiKey || "");
        }
      } catch (err) {
        console.error("Failed to load prompt configs", err);
      } finally {
        setLoading(false);
      }
    };
    fetchConfig();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSuccess(false);

    try {
      const docRef = doc(db, "config", "system");
      
      // Load current document to preserve other keys (like pricing)
      const docSnap = await getDoc(docRef);
      const currentData = docSnap.exists() ? docSnap.data() : {};

      await setDoc(docRef, {
        ...currentData,
        websiteSystemPrompt: websitePrompt,
        appSystemPrompt: appPrompt,
        geminiApiKey: apiKey
      });

      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      console.error("Failed to save config", err);
      alert("Error saving configuration: " + err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: "80px" }}>
        <p>Loading prompt configuration...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="view-header">
        <h1>Prompt & API Config</h1>
        <p>Manage backend system prompts for Gemini queries and update API configurations</p>
      </div>

      <form onSubmit={handleSave} className="config-container">
        {success && <div className="success-banner">System configuration updated successfully!</div>}

        {/* Gemini API Card */}
        <div className="glass-card">
          <h2 className="card-title">Google Gemini Configuration</h2>
          <div className="form-group">
            <label>Gemini API Key (gemini-2.5-flash-lite)</label>
            <input
              type="password"
              className="form-control"
              placeholder="AIzaSy..."
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              required
            />
            <p style={{ fontSize: "11px", color: "var(--text-secondary)", marginTop: "4px" }}>
              Securely stored in Firestore and fetched dynamically by the client at runtime.
            </p>
          </div>
        </div>

        {/* Prompt engineering card */}
        <div className="glass-card">
          <h2 className="card-title">System Instructions Prompt Templates</h2>
          
          <div className="form-group">
            <label>Website Prompt Generator Instructions</label>
            <textarea
              className="form-control"
              value={websitePrompt}
              onChange={(e) => setWebsitePrompt(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Mobile App Prompt Generator Instructions</label>
            <textarea
              className="form-control"
              value={appPrompt}
              onChange={(e) => setAppPrompt(e.target.value)}
              required
            />
          </div>

          <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "24px" }}>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? "Saving settings..." : "Save System Config"}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
