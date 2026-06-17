package com.aipromptgenerater.aitricker.data.model

import com.google.firebase.firestore.PropertyName

data class SystemConfig(
    @get:PropertyName("websiteSystemPrompt") @set:PropertyName("websiteSystemPrompt") var websiteSystemPrompt: String = "You are an expert web development architect. Generate a highly structured, comprehensive development prompt for a website project. The prompt should detail user experience, architecture recommendations, visual design themes, and step-by-step implementation milestones. Output in clear structured text, without any markdown formatting symbols like asterisks (** or *).",
    @get:PropertyName("appSystemPrompt") @set:PropertyName("appSystemPrompt") var appSystemPrompt: String = "You are an expert mobile app development architect. Generate a highly structured, comprehensive development prompt for a mobile app project. The prompt should detail UI flow, components, architectural patterns, and feature scopes. Output in clear structured text, without any markdown formatting symbols like asterisks (** or *).",
    @get:PropertyName("pricePlanBasic") @set:PropertyName("pricePlanBasic") var pricePlanBasic: Int = 99,
    @get:PropertyName("pricePlanPopular") @set:PropertyName("pricePlanPopular") var pricePlanPopular: Int = 299,
    @get:PropertyName("pricePlanPremium") @set:PropertyName("pricePlanPremium") var pricePlanPremium: Int = 499,
    @get:PropertyName("razorpayKeyIdSandbox") @set:PropertyName("razorpayKeyIdSandbox") var razorpayKeyIdSandbox: String = "rzp_test_your_sandbox_key",
    @get:PropertyName("razorpayKeyIdProduction") @set:PropertyName("razorpayKeyIdProduction") var razorpayKeyIdProduction: String = "rzp_live_your_production_key"
)
