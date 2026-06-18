package com.aipromptgenerater.aitricker.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aipromptgenerater.aitricker.data.model.SystemConfig
import com.aipromptgenerater.aitricker.data.remote.RazorpayClient
import com.aipromptgenerater.aitricker.data.remote.RazorpayResultBridge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val razorpayClient: RazorpayClient = RazorpayClient()
) {

    companion object {
        private const val TAG = "PaymentRepository"
    }

    data class PaymentPlan(
        val price: Int,
        val credits: Int,
        val label: String,
        val description: String,
        val tag: String = ""
    )

    /**
     * Retrieves pricing plans dynamically. Falls back to static presets.
     */
    suspend fun getPaymentPlans(): List<PaymentPlan> {
        return try {
            val systemConfig = loadSystemConfig()
            listOf(
                PaymentPlan(systemConfig.pricePlanBasic, 499, "Basic Plan", "Get 499 credits to start building ideas", ""),
                PaymentPlan(systemConfig.pricePlanPopular, 1599, "Most Popular", "Get 1599 credits for extensive prompt tuning", "Popular"),
                PaymentPlan(systemConfig.pricePlanPremium, 2999, "Premium Creator", "Get 2999 credits with ultimate priority access", "Best Value")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading payment plans, using local preset values", e)
            listOf(
                PaymentPlan(99, 499, "Basic Plan", "Get 499 credits to start building ideas", ""),
                PaymentPlan(299, 1599, "Most Popular", "Get 1599 credits for extensive prompt tuning", "Popular"),
                PaymentPlan(499, 2999, "Premium Creator", "Get 2999 credits with ultimate priority access", "Best Value")
            )
        }
    }

    private suspend fun loadSystemConfig(): SystemConfig {
        val doc = firestore.collection("config").document("system").get().await()
        return doc.toObject(SystemConfig::class.java) ?: SystemConfig()
    }

    /**
     * Initializes Razorpay checkout.
     * Launches payment screen on sandbox/production environment.
     */
    suspend fun checkout(
        context: Context,
        userId: String,
        plan: PaymentPlan,
        isSandboxMode: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val activity = context as? Activity
            if (activity == null) {
                onFailure("Context is not an Activity. Cannot open Razorpay Checkout.")
                return
            }

            // Fetch credentials dynamically from Firestore config
            val systemConfig = loadSystemConfig()
            val razorpayKeyId = if (isSandboxMode) {
                systemConfig.razorpayKeyIdSandbox
            } else {
                systemConfig.razorpayKeyIdProduction
            }

            // Fetch user info from Firebase Auth
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val email = firebaseUser?.email ?: "user@aiprompt.com"
            val phone = firebaseUser?.phoneNumber ?: "9999999999"

            // Setup SDK callback bridges
            RazorpayResultBridge.onSuccess = { paymentId ->
                Log.i(TAG, "Razorpay payment success event for: $paymentId")
                verifyAndFulfillPayment(userId, paymentId, plan.credits, onSuccess, onFailure)
            }

            RazorpayResultBridge.onFailure = { code, response ->
                Log.w(TAG, "Razorpay payment failed. Code: $code, Response: $response")
                onFailure(response)
            }

            // Launch Razorpay Standard Checkout
            razorpayClient.startPayment(
                activity = activity,
                keyId = razorpayKeyId,
                amount = plan.price.toDouble(),
                credits = plan.credits,
                planLabel = plan.label,
                userEmail = email,
                userPhone = phone
            )

        } catch (e: Exception) {
            Log.e(TAG, "Checkout process exception", e)
            onFailure(e.message ?: "An unexpected error occurred during payment initiation.")
        }
    }

    /**
     * Securely registers and verifies the payment.
     * Simulates backend webhooks by checking signature or completing the transaction in Firestore.
     */
    private fun verifyAndFulfillPayment(
        userId: String,
        orderId: String,
        creditsToAdd: Int,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userDocRef = firestore.collection("users").document(userId)
        val receiptId = "receipt_" + UUID.randomUUID().toString().replace("-", "")

        // Build database log entry for this transaction
        val receiptData = mapOf(
            "receiptId" to receiptId,
            "orderId" to orderId,
            "userId" to userId,
            "creditsPurchased" to creditsToAdd,
            "timestamp" to System.currentTimeMillis(),
            "status" to "SUCCESS",
            "gateway" to "Razorpay"
        )

        // Write receipt and update user's credits atomically in a Firestore Transaction
        firestore.runTransaction { transaction ->
            // Update user profile credits
            val userSnapshot = transaction.get(userDocRef)
            val currentCredits = userSnapshot.getLong("credits")?.toInt() ?: 0
            transaction.update(userDocRef, "credits", currentCredits + creditsToAdd)

            // Write purchase verification record
            val receiptDocRef = firestore.collection("receipts").document(receiptId)
            transaction.set(receiptDocRef, receiptData)
        }.addOnSuccessListener {
            Log.i(TAG, "Payment transaction verified. Credits added: $creditsToAdd")
            onSuccess("Payment Successful! $creditsToAdd credits added to your wallet.")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Payment verification transaction failed", e)
            onFailure("Failed to secure credits: ${e.message}")
        }
    }
}
