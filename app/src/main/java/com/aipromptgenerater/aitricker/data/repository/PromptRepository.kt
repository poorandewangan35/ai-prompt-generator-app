package com.aipromptgenerater.aitricker.data.repository

import android.util.Log
import com.aipromptgenerater.aitricker.data.model.PromptHistory
import com.aipromptgenerater.aitricker.data.model.SystemConfig
import com.aipromptgenerater.aitricker.data.remote.GeminiClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PromptRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val geminiClient: GeminiClient = GeminiClient()
) {

    companion object {
        private const val TAG = "PromptRepository"
        private const val GEMINI_API_KEY_FALLBACK = "AIzaSyD_EXAMPLE_KEY_FOR_TESTING" // Placeholder
    }

    /**
     * Observable stream of the user's prompt history.
     */
    fun promptHistoryFlow(userId: String): Flow<List<PromptHistory>> = callbackFlow {
        val query = firestore.collection("prompts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching prompt history", error)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val prompts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PromptHistory::class.java)?.apply { id = doc.id }
                }
                trySend(prompts)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * Retrieves dynamic system configurations/prompts.
     */
    suspend fun getSystemConfig(): SystemConfig {
        return try {
            val doc = firestore.collection("config").document("system").get().await()
            if (doc.exists()) {
                doc.toObject(SystemConfig::class.java) ?: SystemConfig()
            } else {
                // Initialize default system config in Firestore if not exists
                val defaultConfig = SystemConfig()
                firestore.collection("config").document("system").set(defaultConfig).await()
                defaultConfig
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading system config from Firestore, fallback to local default", e)
            SystemConfig()
        }
    }

    /**
     * Executes prompt generation via Gemini, updates user's credit balance by subtracting 5 credits,
     * and saves the prompt in history. Evaluated in a batch transaction on the database.
     */
    suspend fun generateAndSavePrompt(
        userId: String,
        type: String, // "Website" or "App"
        name: String,
        idea: String,
        techStack: String,
        features: String
    ): Result<PromptHistory> {
        try {
            // 1. Check user's current credits
            val userDocRef = firestore.collection("users").document(userId)
            val userSnapshot = userDocRef.get().await()
            val currentCredits = userSnapshot.getLong("credits")?.toInt() ?: 0
            if (currentCredits < 5) {
                return Result.failure(Exception("Insufficient credits. You need at least 5 credits to generate a prompt."))
            }

            // 2. Fetch Gemini API key securely from Firestore config (avoiding app exposure)
            val configDoc = firestore.collection("config").document("system").get().await()
            val apiKey = configDoc.getString("geminiApiKey") ?: GEMINI_API_KEY_FALLBACK

            // 3. Fetch system instruction
            val systemConfig = getSystemConfig()
            val systemInstruction = if (type.lowercase() == "app") {
                systemConfig.appSystemPrompt
            } else {
                systemConfig.websiteSystemPrompt
            }

            // 4. Construct Gemini prompt payload
            val userPromptBuilder = StringBuilder().apply {
                if (name.isNotEmpty()) append("Project Name: $name\n")
                append("Core Idea: $idea\n")
                if (techStack.isNotEmpty()) append("Preferred Tech Stack: $techStack\n")
                if (features.isNotEmpty()) append("Core Features List: $features\n")
            }

            // 5. Call Gemini API
            val geminiResult = geminiClient.generatePrompt(
                apiKey = apiKey,
                systemInstruction = systemInstruction,
                userPrompt = userPromptBuilder.toString()
            )

            if (geminiResult.isFailure) {
                return Result.failure(geminiResult.exceptionOrNull() ?: Exception("Gemini API generation failed"))
            }

            val generatedResponse = geminiResult.getOrThrow()

            // 6. Execute atomic batch write: decrement credits and write prompt history
            val promptId = "prompt_" + UUID.randomUUID().toString().replace("-", "")
            val promptHistory = PromptHistory(
                id = promptId,
                userId = userId,
                type = type,
                name = name,
                idea = idea,
                techStack = techStack,
                features = features,
                response = generatedResponse,
                createdAt = System.currentTimeMillis()
            )

            firestore.runTransaction { transaction ->
                // Fetch user document inside transaction for safety
                val userSnapshotInTx = transaction.get(userDocRef)
                val creditsInTx = userSnapshotInTx.getLong("credits")?.toInt() ?: 0
                if (creditsInTx < 5) {
                    throw Exception("Tx Error: Insufficient credits")
                }

                // Decrement credits
                transaction.update(userDocRef, "credits", creditsInTx - 5)

                // Write prompt document
                val promptDocRef = firestore.collection("prompts").document(promptId)
                transaction.set(promptDocRef, promptHistory)
            }.await()

            return Result.success(promptHistory)

        } catch (e: Exception) {
            Log.e(TAG, "Failed prompt generation sequence", e)
            return Result.failure(e)
        }
    }
}
