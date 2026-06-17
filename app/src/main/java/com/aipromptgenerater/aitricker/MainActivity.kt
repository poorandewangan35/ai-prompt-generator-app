package com.aipromptgenerater.aitricker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aipromptgenerater.aitricker.data.remote.RazorpayResultBridge
import com.aipromptgenerater.aitricker.theme.ThemeManager
import com.aipromptgenerater.aitricker.theme.AiPromptGeneraterAppBuilderTheme
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener

class MainActivity : ComponentActivity(), PaymentResultListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Theme preference
        ThemeManager.initialize(applicationContext)
        
        // Preload Razorpay Checkout resources
        Checkout.preload(applicationContext)

        enableEdgeToEdge()
        setContent {
            AiPromptGeneraterAppBuilderTheme(darkTheme = ThemeManager.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        RazorpayResultBridge.onSuccess?.invoke(razorpayPaymentId ?: "Success")
    }

    override fun onPaymentError(code: Int, response: String?) {
        RazorpayResultBridge.onFailure?.invoke(code, response ?: "Payment cancelled or failed")
    }
}
