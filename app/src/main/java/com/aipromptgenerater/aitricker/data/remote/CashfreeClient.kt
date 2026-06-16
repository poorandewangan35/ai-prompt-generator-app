package com.aipromptgenerater.aitricker.data.remote

import android.content.Context
import android.util.Log
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.CFTheme
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.exception.CFException
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.ui.api.CFDropCheckoutPayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

class CashfreeClient {

    companion object {
        private const val TAG = "CashfreeClient"
    }

    data class OrderResponse(
        val orderId: String,
        val paymentSessionId: String,
        val cfEnvironment: String // "SANDBOX" or "PRODUCTION"
    )

    /**
     * Simulates backend order creation for Cashfree.
     * Returns mock session details.
     */
    suspend fun createOrderMock(amount: Double, credits: Int): OrderResponse = withContext(Dispatchers.IO) {
        delay(1000) // Simulate network latency
        val orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
        val mockSessionId = "session_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20)
        return@withContext OrderResponse(
            orderId = orderId,
            paymentSessionId = mockSessionId,
            cfEnvironment = "SANDBOX"
        )
    }

    /**
     * Production implementation for creating orders.
     * Hits your backend server endpoint which secures the Cashfree Secret Key.
     */
    suspend fun createOrderProduction(backendUrl: String, amount: Double, userId: String): Result<OrderResponse> = withContext(Dispatchers.IO) {
        // Under production, you call your server API. Example:
        // Retrofit client calling POST /api/create-order
        try {
            // Placeholder: Simulate production call or write boilerplate
            val orderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
            val mockSessionId = "session_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20)
            Result.success(OrderResponse(orderId, mockSessionId, "SANDBOX"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Triggers Cashfree PG SDK Checkout screen.
     */
    fun startPayment(
        context: Context,
        orderId: String,
        paymentSessionId: String,
        isSandbox: Boolean,
        callback: CFCheckoutResponseCallback
    ) {
        val environment = if (isSandbox) {
            CFSession.Environment.SANDBOX
        } else {
            CFSession.Environment.PRODUCTION
        }

        // Create Cashfree session
        val session = CFSession.CFSessionBuilder()
            .setEnvironment(environment)
            .setPaymentSessionID(paymentSessionId)
            .setOrderId(orderId)
            .build()

        // Initialize payment launch
        val checkoutPayment = CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
            .setSession(session)
            .build()

        val gatewayService = CFPaymentGatewayService.getInstance()
        gatewayService.setCheckoutCallback(callback)
        gatewayService.doPayment(context, checkoutPayment)
    }
}
