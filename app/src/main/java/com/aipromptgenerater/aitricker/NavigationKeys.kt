package com.aipromptgenerater.aitricker

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data class Generator(val type: String) : NavKey // type can be "Website" or "App"

@Serializable
data object Wallet : NavKey

@Serializable
data object History : NavKey

@Serializable
data object Profile : NavKey

@Serializable
data object PaymentHistory : NavKey
