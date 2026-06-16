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

class GeminiClient {

    companion object {
        private const val TAG = "GeminiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"
    }

    /**
     * Generates prompt content using gemini-2.5-flash-lite.
     * Extracts plain text and cleans markdown tags (e.g. **, *, #) as requested.
     */
    suspend fun generatePrompt(
        apiKey: String,
        systemInstruction: String,
        userPrompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlString = "$BASE_URL?key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Build request JSON
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "$systemInstruction\n\nUser Input: $userPrompt") })
                        })
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                
                // Add system instructions if supported by endpoint, or combine in text
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topK", 40)
                    put("topP", 0.95)
                })
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
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val rawText = parts.getJSONObject(0).optString("text")
                        val cleanText = sanitizeMarkdown(rawText)
                        return@withContext Result.success(cleanText)
                    }
                }
                return@withContext Result.failure(Exception("Empty response parts from Gemini API"))
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream))
                val errorStringBuilder = StringBuilder()
                var line: String?
                while (errorReader.readLine().also { line = it } != null) {
                    errorStringBuilder.append(line)
                }
                errorReader.close()
                Log.e(TAG, "Error Response: ${errorStringBuilder.toString()}")
                return@withContext Result.failure(Exception("Gemini API Error ($responseCode): ${connection.responseMessage}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during content generation", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Sanitizes markdown text by stripping tags as required: ** bold, * bullet, # headers, etc.
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
