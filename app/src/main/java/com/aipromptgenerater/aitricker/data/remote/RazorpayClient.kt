package com.aipromptgenerater.aitricker.data.remote

import android.app.Activity
import android.util.Log
import com.razorpay.Checkout
import org.json.JSONObject

class RazorpayClient {

    companion object {
        private const val TAG = "RazorpayClient"
    }

    /**
     * Triggers Razorpay SDK Checkout screen.
     */
    fun startPayment(
        activity: Activity,
        keyId: String,
        amount: Double,
        credits: Int,
        planLabel: String,
        userEmail: String,
        userPhone: String
    ) {
        val checkout = Checkout()
        checkout.setKeyID(keyId)

        try {
            val options = JSONObject()
            options.put("name", "AI Prompt Generator")
            options.put("description", planLabel)
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg") // Standard Razorpay logo
            options.put("theme.color", "#2563EB") // Royal blue theme matching the app's premium aesthetics
            options.put("currency", "INR")
            
            // Razorpay amount is in paise (e.g. ₹99 = 9900 paise)
            options.put("amount", (amount * 100).toInt())

            // Prefill info
            val prefill = JSONObject()
            prefill.put("email", userEmail)
            prefill.put("contact", userPhone)
            options.put("prefill", prefill)

            // Custom metadata / notes
            val notes = JSONObject()
            notes.put("credits", credits)
            notes.put("planLabel", planLabel)
            options.put("notes", notes)

            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e)
            RazorpayResultBridge.onFailure?.invoke(-1, e.message ?: "SDK Initiation error")
        }
    }
}

/**
 * A bridge object to route payment success/failure events from MainActivity back to Repository.
 */
object RazorpayResultBridge {
    var onSuccess: ((String) -> Unit)? = null
    var onFailure: ((Int, String) -> Unit)? = null
}
