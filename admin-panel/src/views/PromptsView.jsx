import React, { useEffect, useState } from "react";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { db } from "../firebase";

export default function PromptsView() {
  const [websitePrompt, setWebsitePrompt] = useState("");
  const [appPrompt, setAppPrompt] = useState("");
  const [openRouterApiKey, setOpenRouterApiKey] = useState("");
  const [openRouterModel, setOpenRouterModel] = useState("google/gemini-2.0-flash-lite:free");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  
  // Model testing states
  const [testingModel, setTestingModel] = useState(false);
  const [testStatus, setTestStatus] = useState(""); // success, error, ""
  const [testErrorMsg, setTestErrorMsg] = useState("");

  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const docRef = doc(db, "config", "system");
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          const data = docSnap.data();
          setWebsitePrompt(data.websiteSystemPrompt || "");
          setAppPrompt(data.appSystemPrompt || "");
          setOpenRouterApiKey(data.openRouterApiKey || "");
          setOpenRouterModel(data.openRouterModel || "google/gemini-2.0-flash-lite:free");
        }
      } catch (err) {
        console.error("Failed to load prompt configs", err);
      } finally {
        setLoading(false);
      }
    };
    fetchConfig();
  }, []);

  const handleTestModel = async () => {
    if (!openRouterApiKey) {
      alert("Please enter your OpenRouter API Key first.");
      return;
    }
    if (!openRouterModel) {
      alert("Please enter a Model ID to test.");
      return;
    }
    setTestingModel(true);
    setTestStatus("");
    setTestErrorMsg("");

    try {
      const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${openRouterApiKey}`,
          "HTTP-Referer": "https://github.com/poorandewangan35/ai-prompt-generator-app",
          "X-Title": "AI Prompt Generator Admin Panel"
        },
        body: JSON.stringify({
          model: openRouterModel,
          messages: [
            { role: "user", content: "Hello! This is a test request. Please respond with exactly 'Success'." }
          ]
        })
      });

      if (response.ok) {
        const data = await response.json();
        const content = data?.choices?.[0]?.message?.content;
        if (content) {
          setTestStatus("success");
        } else {
          setTestStatus("error");
          setTestErrorMsg("Empty response choices received from OpenRouter.");
        }
      } else {
        const errData = await response.json().catch(() => ({}));
        setTestStatus("error");
        setTestErrorMsg(errData?.error?.message || `HTTP Error ${response.status}: ${response.statusText}`);
      }
    } catch (err) {
      console.error("Error testing model", err);
      setTestStatus("error");
      setTestErrorMsg(err.message || "Network error occurred.");
    } finally {
      setTestingModel(false);
    }
  };

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
        openRouterApiKey: openRouterApiKey,
        openRouterModel: openRouterModel
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

        {/* OpenRouter Configuration Card */}
        <div className="glass-card">
          <h2 className="card-title">OpenRouter AI Configuration</h2>
          
          <div className="form-group" style={{ marginBottom: "20px" }}>
            <label>OpenRouter API Key</label>
            <input
              type="password"
              className="form-control"
              placeholder="sk-or-v1-..."
              value={openRouterApiKey}
              onChange={(e) => setOpenRouterApiKey(e.target.value)}
              required
            />
            <p style={{ fontSize: "11px", color: "var(--text-secondary)", marginTop: "4px" }}>
              Securely stored in Firestore and fetched dynamically by the client at runtime.
            </p>
          </div>

          <div className="form-group">
            <label>AI Model (OpenRouter)</label>
            <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
              <input
                type="text"
                className="form-control"
                placeholder="google/gemini-2.0-flash-lite:free"
                value={openRouterModel}
                onChange={(e) => setOpenRouterModel(e.target.value)}
                style={{ flex: 1 }}
                required
              />
              <button
                type="button"
                className="btn-secondary"
                onClick={handleTestModel}
                disabled={testingModel}
                style={{
                  padding: "10px 18px",
                  borderRadius: "8px",
                  backgroundColor: "rgba(255, 255, 255, 0.08)",
                  border: "1px solid rgba(255, 255, 255, 0.1)",
                  color: "#fff",
                  cursor: "pointer",
                  fontWeight: "600",
                  whiteSpace: "nowrap"
                }}
              >
                {testingModel ? "Testing..." : "Test Model"}
              </button>
            </div>
            <p style={{ fontSize: "11px", color: "var(--text-secondary)", marginTop: "4px" }}>
              Model ID from OpenRouter. Examples: google/gemini-2.0-flash-lite:free, z-ai/glm-4.5-air:free, openai/gpt-4o-mini
            </p>
          </div>

          {testStatus === "success" && (
            <div style={{
              marginTop: "12px",
              padding: "10px 14px",
              borderRadius: "8px",
              backgroundColor: "rgba(45, 212, 191, 0.1)",
              color: "#2dd4bf",
              fontSize: "13px",
              fontWeight: "600"
            }}>
              ✔ Model tested successfully!
            </div>
          )}

          {testStatus === "error" && (
            <div style={{
              marginTop: "12px",
              padding: "10px 14px",
              borderRadius: "8px",
              backgroundColor: "rgba(239, 68, 68, 0.1)",
              color: "#ef4444",
              fontSize: "13px",
              fontWeight: "600"
            }}>
              ✘ Model test failed: {testErrorMsg}
            </div>
          )}
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
