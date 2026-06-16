package com.aipromptgenerater.aitricker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.aipromptgenerater.aitricker.theme.GradientEnd
import com.aipromptgenerater.aitricker.theme.GradientMiddle
import com.aipromptgenerater.aitricker.theme.GradientStart

@Composable
fun GradientBackground(
    content: @Composable () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            GradientStart,
            GradientMiddle,
            GradientEnd
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        content()
    }
}
