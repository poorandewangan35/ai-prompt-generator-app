package com.aipromptgenerater.aitricker.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class OpenRouterClient {

    companion object {
        private const val TAG = "OpenRouterClient"
        private const val BASE_URL = "https://openrouter.ai/api/v1/chat/completions"
    }

    /**
     * Generates prompt content using OpenRouter API.
     * Uses specified model and system instruction.
     */
    suspend fun generatePrompt(
        apiKey: String,
        model: String,
        systemInstruction: String,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("HTTP-Referer", "https://github.com/poorandewangan35/ai-prompt-generator-app")
            connection.setRequestProperty("X-Title", "AI Prompt Generator")
            connection.doOutput = true

            // Build OpenAI-compatible chat request JSON
            val requestJson = JSONObject().apply {
                put("model", model)
                
                val messagesArray = JSONArray().apply {
                    // System prompt message
                    val systemMessage = JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    }
                    put(systemMessage)
                    
                    // User prompt message
                    val userMessage = JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    }
                    put(userMessage)
                }
                put("messages", messagesArray)
            }

            // Write output stream
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseReader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseStringBuilder = StringBuilder()
                var line: String?
                while (responseReader.readLine().also { line = it } != null) {
                    responseStringBuilder.append(line)
                }
                responseReader.close()

                val responseJson = JSONObject(responseStringBuilder.toString())
                val choices = responseJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val messageObj = firstChoice.optJSONObject("message")
                    val rawText = messageObj?.optString("content") ?: ""
                    val cleanText = sanitizeMarkdown(rawText)
                    return@withContext Result.success(cleanText)
                }
                return@withContext Result.failure(Exception("Empty response choices from OpenRouter API"))
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
                val errorStringBuilder = StringBuilder()
                var line: String?
                while (errorReader.readLine().also { line = it } != null) {
                    errorStringBuilder.append(line)
                }
                errorReader.close()
                Log.e(TAG, "Error Response: ${errorStringBuilder.toString()}")
                return@withContext Result.failure(Exception("OpenRouter API Error ($responseCode): ${connection.responseMessage}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during content generation via OpenRouter", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Sanitizes markdown text by stripping tags: ** bold, * bullet, # headers, etc.
     */
    private fun sanitizeMarkdown(markdown: String): String {
        return markdown
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Remove bold markdown **bold** -> bold
            .replace(Regex("\\*(.*?)\\*"), "$1")       // Remove italic markdown *italic* -> italic
            .replace(Regex("__(.*?)__"), "$1")         // Remove underscore bold
            .replace(Regex("_(.*?)_"), "$1")           // Remove underscore italic
            .replace(Regex("`{3,}[a-zA-Z]*\\n?([\\s\\S]*?)\\n?`{3,}"), "$1") // Clean code blocks
            .replace(Regex("`(.*?)`"), "$1")           // Clean inline code tags
            .replace(Regex("(?m)^#+\\s*(.*)$"), "$1")  // Remove markdown headers
            .replace(Regex("(?m)^-\\s+(.*)$"), "• $1") // Normalize lists to use clean bullet points
    }
}
