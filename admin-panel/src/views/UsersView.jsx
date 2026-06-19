import React, { useEffect, useState } from "react";
import { collection, onSnapshot, doc, updateDoc } from "firebase/firestore";
import { db, auth } from "../firebase";
import { Search, Edit2 } from "lucide-react";

export default function UsersView() {
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  
  // Selected user for credit editing modal
  const [editingUser, setEditingUser] = useState(null);
  const [editCreditsValue, setEditCreditsValue] = useState(15);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    if (!auth.currentUser) {
      // Mock user database
      setUsers([
        { id: "u1", displayName: "Aman Sharma", email: "aman@gmail.com", createdAt: Date.now() - 7200000, credits: 45 },
        { id: "u2", displayName: "Neha Verma", email: "neha.v@yahoo.com", createdAt: Date.now() - 172800000, credits: 15 },
        { id: "u3", displayName: "Rahul Kumar", email: "rahul99@outlook.com", createdAt: Date.now() - 432000000, credits: 120 },
        { id: "u4", displayName: "Priya Patel", email: "priya_patel@gmail.com", createdAt: Date.now() - 691200000, credits: 0 },
        { id: "u5", displayName: "Vikram Singh", email: "vikram.singh@gmx.com", createdAt: Date.now() - 1036800000, credits: 75 }
      ]);
      setLoading(false);
      return;
    }

    const unsubscribe = onSnapshot(collection(db, "users"), (snapshot) => {
      const list = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setUsers(list);
      setLoading(false);
    }, (err) => {
      console.error("Failed to fetch user list, using fallback:", err);
      setUsers([
        { id: "u1", displayName: "Aman Sharma", email: "aman@gmail.com", createdAt: Date.now() - 7200000, credits: 45 },
        { id: "u2", displayName: "Neha Verma", email: "neha.v@yahoo.com", createdAt: Date.now() - 172800000, credits: 15 },
        { id: "u3", displayName: "Rahul Kumar", email: "rahul99@outlook.com", createdAt: Date.now() - 432000000, credits: 120 }
      ]);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const openEditModal = (user) => {
    setEditingUser(user);
    setEditCreditsValue(user.credits ?? 15);
  };

  const closeEditModal = () => {
    setEditingUser(null);
  };

  const handleUpdateCredits = async (e) => {
    e.preventDefault();
    if (!editingUser) return;
    setUpdating(true);

    try {
      const userRef = doc(db, "users", editingUser.id);
      await updateDoc(userRef, {
        credits: Number(editCreditsValue)
      });
      closeEditModal();
    } catch (err) {
      console.error("Failed to update user credits", err);
      alert("Error updating credits: " + err.message);
    } finally {
      setUpdating(false);
    }
  };

  const filteredUsers = users.filter((u) => 
    u.email?.toLowerCase().includes(search.toLowerCase()) || 
    u.displayName?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="view-header">
        <h1>User Account Manager</h1>
        <p>Monitor user accounts, registration records, and manually manage wallet balances</p>
      </div>

      <div className="glass-card" style={{ marginBottom: "20px" }}>
        {/* Search Bar */}
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <Search size={20} style={{ color: "var(--text-secondary)" }} />
          <input
            type="text"
            className="form-control"
            placeholder="Search users by email or name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      <div className="glass-card">
        {loading ? (
          <p style={{ textAlign: "center", padding: "40px" }}>Loading user database...</p>
        ) : filteredUsers.length === 0 ? (
          <p style={{ textAlign: "center", padding: "40px", color: "var(--text-secondary)" }}>
            No matching users found.
          </p>
        ) : (
          <div className="table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Display Name</th>
                  <th>Email</th>
                  <th>Registered Date</th>
                  <th>Wallet Credits</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td style={{ fontWeight: "600" }}>{user.displayName || "AI User"}</td>
                    <td>{user.email}</td>
                    <td>{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "N/A"}</td>
                    <td style={{ fontWeight: "700", color: "#3b82f6" }}>{user.credits ?? 0} Credits</td>
                    <td>
                      <button 
                        className="btn-secondary" 
                        style={{ padding: "8px 16px", fontSize: "12px", display: "flex", alignItems: "center", gap: "6px" }}
                        onClick={() => openEditModal(user)}
                      >
                        <Edit2 size={12} /> Edit Credits
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Credit adjuster modal overlay */}
      {editingUser && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2 className="card-title">Adjust Wallet Balance</h2>
            <p style={{ fontSize: "13px", color: "var(--text-secondary)", marginBottom: "20px" }}>
              User: <strong>{editingUser.email}</strong>
            </p>
            
            <form onSubmit={handleUpdateCredits}>
              <div className="form-group">
                <label>Set Credits Count</label>
                <input
                  type="number"
                  className="form-control"
                  value={editCreditsValue}
                  onChange={(e) => setEditCreditsValue(e.target.value)}
                  required
                  min="0"
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={closeEditModal} disabled={updating}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={updating}>
                  {updating ? "Saving..." : "Apply Adjustments"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
