import React, { useEffect, useState } from "react";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { db } from "../firebase";

export default function PricingView() {
  const [priceBasic, setPriceBasic] = useState(99);
  const [pricePopular, setPricePopular] = useState(299);
  const [pricePremium, setPricePremium] = useState(499);
  const [razorpayKeySandbox, setRazorpayKeySandbox] = useState("");
  const [razorpayKeyProduction, setRazorpayKeyProduction] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const fetchPricing = async () => {
      try {
        const docRef = doc(db, "config", "system");
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          const data = docSnap.data();
          setPriceBasic(data.pricePlanBasic ?? 99);
          setPricePopular(data.pricePlanPopular ?? 299);
          setPricePremium(data.pricePlanPremium ?? 499);
          setRazorpayKeySandbox(data.razorpayKeyIdSandbox ?? "");
          setRazorpayKeyProduction(data.razorpayKeyIdProduction ?? "");
        }
      } catch (err) {
        console.error("Failed to load pricing and payment config", err);
      } finally {
        setLoading(false);
      }
    };
    fetchPricing();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSuccess(false);

    try {
      const docRef = doc(db, "config", "system");
      
      // Load current document to preserve other keys (like prompts and api keys)
      const docSnap = await getDoc(docRef);
      const currentData = docSnap.exists() ? docSnap.data() : {};

      await setDoc(docRef, {
        ...currentData,
        pricePlanBasic: Number(priceBasic),
        pricePlanPopular: Number(pricePopular),
        pricePlanPremium: Number(pricePremium),
        razorpayKeyIdSandbox: razorpayKeySandbox,
        razorpayKeyIdProduction: razorpayKeyProduction
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
        <p>Loading configuration...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="view-header">
        <h1>System Settings Manager</h1>
        <p>Dynamically update pricing rates, packages, and payment gateway keys for the Mobile Credit Store</p>
      </div>

      <form onSubmit={handleSave} className="config-container">
        {success && <div className="success-banner">System configuration updated successfully!</div>}

        <div className="glass-card">
          <h2 className="card-title">Active Pricing Packages</h2>
          
          <div className="form-group">
            <label>Basic Package Price (₹) • 499 Credits</label>
            <input
              type="number"
              className="form-control"
              value={priceBasic}
              onChange={(e) => setPriceBasic(e.target.value)}
              required
              min="1"
            />
          </div>

          <div className="form-group">
            <label>Most Popular Package Price (₹) • 1599 Credits</label>
            <input
              type="number"
              className="form-control"
              value={pricePopular}
              onChange={(e) => setPricePopular(e.target.value)}
              required
              min="1"
            />
          </div>

          <div className="form-group">
            <label>Premium Package Price (₹) • 2999 Credits</label>
            <input
              type="number"
              className="form-control"
              value={pricePremium}
              onChange={(e) => setPricePremium(e.target.value)}
              required
              min="1"
            />
          </div>
        </div>

        <div className="glass-card" style={{ marginTop: "24px" }}>
          <h2 className="card-title">Razorpay Gateway Configurations</h2>
          
          <div className="form-group">
            <label>Razorpay Key ID (Sandbox / Test Mode)</label>
            <input
              type="text"
              className="form-control"
              value={razorpayKeySandbox}
              onChange={(e) => setRazorpayKeySandbox(e.target.value)}
              placeholder="rzp_test_xxxxxxxxxxxxxx"
            />
          </div>

          <div className="form-group">
            <label>Razorpay Key ID (Production / Live Mode)</label>
            <input
              type="text"
              className="form-control"
              value={razorpayKeyProduction}
              onChange={(e) => setRazorpayKeyProduction(e.target.value)}
              placeholder="rzp_live_xxxxxxxxxxxxxx"
            />
          </div>
        </div>

        <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "24px" }}>
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? "Updating config..." : "Save Settings"}
          </button>
        </div>
      </form>
    </div>
  );
}
