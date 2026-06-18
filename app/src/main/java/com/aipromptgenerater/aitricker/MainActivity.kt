package com.aipromptgenerater.aitricker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aipromptgenerater.aitricker.data.remote.RazorpayResultBridge
import com.aipromptgenerater.aitricker.theme.ThemeManager
import com.aipromptgenerater.aitricker.theme.AiPromptGeneraterAppBuilderTheme
import com.aipromptgenerater.aitricker.ui.splash.SplashScreen
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
            var showSplash by remember { mutableStateOf(true) }

            AiPromptGeneraterAppBuilderTheme(darkTheme = ThemeManager.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen(onTimeout = { showSplash = false })
                    } else {
                        MainNavigation()
                    }
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
