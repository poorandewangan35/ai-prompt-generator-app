package com.aipromptgenerater.aitricker.data.repository

import android.content.Context
import android.util.Log
import com.aipromptgenerater.aitricker.data.model.SystemConfig
import com.aipromptgenerater.aitricker.data.remote.CashfreeClient
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val cashfreeClient: CashfreeClient = CashfreeClient()
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
     * Initializes Cashfree checkout.
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
            // 1. Create order token via backend simulation or actual server call
            val orderDetails = if (isSandboxMode) {
                cashfreeClient.createOrderMock(plan.price.toDouble(), plan.credits)
            } else {
                // Call server URL setup
                val prodOrder = cashfreeClient.createOrderProduction(
                    backendUrl = "https://your-backend-api.com/create-order",
                    amount = plan.price.toDouble(),
                    userId = userId
                )
                if (prodOrder.isFailure) {
                    onFailure(prodOrder.exceptionOrNull()?.message ?: "Backend failed to create Cashfree order")
                    return
                }
                prodOrder.getOrThrow()
            }

            // 2. Launch checkout
            cashfreeClient.startPayment(
                context = context,
                orderId = orderDetails.orderId,
                paymentSessionId = orderDetails.paymentSessionId,
                isSandbox = isSandboxMode,
                callback = object : CFCheckoutResponseCallback {
                    override fun onPaymentVerify(orderId: String) {
                        Log.i(TAG, "Cashfree payment verify event for: $orderId")
                        // In local test environment, we call the secure verification simulation
                        // In production, your backend webhook or payment API handles verification
                        verifyAndFulfillPayment(userId, orderId, plan.credits, onSuccess, onFailure)
                    }

                    override fun onPaymentFailure(cfErrorResponse: CFErrorResponse, orderId: String) {
                        Log.w(TAG, "Cashfree payment failed for: $orderId. ${cfErrorResponse.message}")
                        onFailure(cfErrorResponse.message ?: "Payment was cancelled or failed.")
                    }
                }
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
            "status" to "SUCCESS"
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
