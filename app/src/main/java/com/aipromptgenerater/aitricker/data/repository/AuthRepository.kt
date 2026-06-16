package com.aipromptgenerater.aitricker.data.repository

import android.util.Log
import com.aipromptgenerater.aitricker.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Observable stream of the current firebase user authentication state.
     */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Observable stream of the user's Firestore profile (e.g. credits).
     */
    fun userProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to user profile changes", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Checks if a user profile exists. If not, creates one with 15 credits securely.
     */
    suspend fun checkAndCreateUserProfile(user: FirebaseUser): UserProfile {
        val docRef = firestore.collection("users").document(user.uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java) ?: UserProfile(uid = user.uid, email = user.email ?: "")
            } else {
                // Initialize user with 15 credits
                val newProfile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "AI Creator",
                    credits = 15,
                    createdAt = System.currentTimeMillis()
                )
                docRef.set(newProfile).await()
                newProfile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/creating user profile in Firestore", e)
            // Return placeholder if Firestore write fails (will be caught by rules/validation)
            UserProfile(uid = user.uid, email = user.email ?: "", credits = 15)
        }
    }

    /**
     * Signs out the user.
     */
    fun signOut() {
        auth.signOut()
    }
}
