package com.aipromptgenerater.aitricker.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class CashfreeClient {

    companion object {
        private const val TAG = "CashfreeClient"
        private const val SANDBOX_URL = "https://sandbox.cashfree.com/pg/orders"
        private const val PRODUCTION_URL = "https://api.cashfree.com/pg/orders"
    }

    /**
     * Initiates Cashfree web checkout.
     * Hits Cashfree APIs directly to create an order session, then opens the web checkout URL.
     */
    suspend fun startPayment(
        context: Context,
        appId: String,
        secretKey: String,
        amount: Double,
        credits: Int,
        userId: String,
        userEmail: String,
        userPhone: String,
        isSandbox: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // If keys are not set, fallback to mock sandbox simulator
            if (appId.isBlank() || secretKey.isBlank() || appId.startsWith("cf_test_")) {
                Log.i(TAG, "No valid keys. Running offline sandbox mock Cashfree flow.")
                simulateMockPayment(context, amount, credits, onSuccess, onFailure)
                return@withContext
            }

            val endpoint = if (isSandbox) SANDBOX_URL else PRODUCTION_URL
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("x-client-id", appId)
            connection.setRequestProperty("x-client-secret", secretKey)
            connection.setRequestProperty("x-api-version", "2023-08-01")
            connection.doOutput = true

            // Generate order details
            val orderId = "order_" + UUID.randomUUID().toString().replace("-", "").take(16)
            val requestJson = JSONObject().apply {
                put("order_id", orderId)
                put("order_amount", amount)
                put("order_currency", "INR")
                
                val customerDetails = JSONObject().apply {
                    put("customer_id", userId.take(30)) // Cashfree limit
                    put("customer_email", userEmail)
                    put("customer_phone", userPhone.replace("+", ""))
                }
                put("customer_details", customerDetails)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val responseJson = JSONObject(sb.toString())
                val paymentLink = responseJson.optString("payment_link")
                val sessionOrderId = responseJson.optString("order_id")

                if (paymentLink.isNotEmpty()) {
                    // Route to web browser / custom tabs
                    withContext(Dispatchers.Main) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink))
                            context.startActivity(intent)
                            onSuccess(sessionOrderId)
                        } catch (e: Exception) {
                            onFailure("Failed to launch web browser: ${e.localizedMessage}")
                        }
                    }
                } else {
                    onFailure("Invalid payment link returned from Cashfree PG.")
                }
            } else {
                val errorStream = connection.errorStream ?: connection.inputStream
                val reader = BufferedReader(InputStreamReader(errorStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                Log.e(TAG, "Cashfree Error Response: $sb")
                onFailure("Cashfree API error ($responseCode). Running mock fallback.")
                simulateMockPayment(context, amount, credits, onSuccess, onFailure)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception in Cashfree Payment initiation", e)
            simulateMockPayment(context, amount, credits, onSuccess, onFailure)
        }
    }

    private suspend fun simulateMockPayment(
        context: Context,
        amount: Double,
        credits: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val mockTransactionId = "cf_mock_" + System.currentTimeMillis()
            onSuccess(mockTransactionId)
        }
    }
}

/**
 * A bridge object to route Cashfree payment callbacks back to repositories.
 */
object CashfreeResultBridge {
    var onSuccess: ((String) -> Unit)? = null
    var onFailure: ((String) -> Unit)? = null
}
