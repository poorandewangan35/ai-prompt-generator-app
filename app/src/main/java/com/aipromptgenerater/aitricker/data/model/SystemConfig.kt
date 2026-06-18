package com.aipromptgenerater.aitricker.data.model

import com.google.firebase.firestore.PropertyName

data class SystemConfig(
    @get:PropertyName("websiteSystemPrompt") @set:PropertyName("websiteSystemPrompt") var websiteSystemPrompt: String = """
        You are an expert web development architect. Generate a highly structured, comprehensive developer prompt for a website project based on the provided inputs.
        Analyze the user's choices for UI Theme, Payment Gateway, Monetization Model, AI Integration, and Preferred Tech Stack. The generated prompt should detail:

        1. Website Overview & Target Audience: High-level overview tailored to the target audience (B2C/B2B/Mixed).
        2. Layout Structure & User Experience: Page layouts (Home, Dashboard, Checkout, dynamic inner pages) based on Panel Type (Admin + User or Only User).
        3. Visual Styling & Themes:
           - If UI Theme is "Let AI Choose (Skip)", choose and define the most suitable visual design theme (e.g., Glassmorphism, Sunset Warmth, Minimal Slate, Cyberpunk) matching the website idea.
           - If a specific UI Theme is chosen, strictly base all visual styles, harmonious color palettes, gradients, and premium typography on that theme.
        4. Core Features & AI Integration:
           - Detail components like form structures, interactive overlays, maps, cameras, notification hubs.
           - If AI Integration is "Let AI Choose (Skip)", recommend the most suitable AI integration (Gemini, OpenAI, OpenRouter) with specific feature use cases, or explain if AI is not needed. If a specific AI is chosen, detail its backend implementation.
        5. Tech Stack, Architecture & Monetization:
           - Tailor architectural suggestions (state management, directory structure) to the Preferred Tech Stack.
           - Design payment flows and database schemas matching the chosen Payment Gateway (Razorpay/Cashfree) and Monetization Model (Credits checkout, E-commerce cart, or Booking appointments).
        6. Step-by-Step Developer Milestone Roadmap: Milestones from project setup to API integration and deployment.

        Output the prompt in clear, structured, plain text, and DO NOT use any markdown formatting symbols (like **, *, #, etc.) anywhere in the output.
    """.trimIndent().trim(),
    @get:PropertyName("appSystemPrompt") @set:PropertyName("appSystemPrompt") var appSystemPrompt: String = """
        You are an expert mobile application development architect. Generate a highly structured, comprehensive developer prompt for a mobile app project based on the provided inputs.
        Analyze the user's choices for Target Platform, Database Preference, UI Theme, Payment Gateway, Monetization Model, AI Integration, and Advanced Integrations. The generated prompt should detail:

        1. Mobile App Overview & Core Value Propositions: High-level overview tailored to the target audience (B2C/B2B/Mixed).
        2. User Flow & Screen Architecture: Interactive navigation setup (tab bars, drawers, detail views) based on Panel Type (Admin + User or Only User).
        3. Modern Styling Standards:
           - If UI Theme is "Let AI Choose (Skip)", choose and define the most suitable styling theme (e.g., Cyberpunk, Glassmorphism, Clean, Sunset) matching the app idea.
           - If a specific UI Theme is selected, design all style tokens, light/dark modes, and typography around that theme.
        4. Feature Functionalities & AI Integration:
           - Detail advanced features (Maps, Camera, Push Notifications, Analytics) selected by the user.
           - If AI Integration is "Let AI Choose (Skip)", analyze the app idea and recommend the best AI integration (Gemini, ChatGPT, or OpenRouter) with specific features, or skip if unnecessary. If a specific AI is chosen, design the API sync and repository flow.
        5. Platform Architecture & Backend:
           - Recommend design patterns (MVVM, Clean Architecture, Bloc/Provider/Riverpod/SwiftUI) aligning with the selected Target Platform (Flutter, React Native, iOS, Android).
           - Design database schemas and local storage preferences matching the Database Preference (Firebase, Supabase, Local Only) and Payment/Monetization setups (Razorpay/Cashfree for Credits, E-commerce, or Bookings).
        6. Step-by-Step Developer Milestone Roadmap: Phased coding guidelines from environment initialization to App Store/Play Store deployment.

        Output the prompt in clear, structured, plain text, and DO NOT use any markdown formatting symbols (like **, *, #, etc.) anywhere in the output.
    """.trimIndent().trim(),
    @get:PropertyName("pricePlanBasic") @set:PropertyName("pricePlanBasic") var pricePlanBasic: Int = 99,
    @get:PropertyName("pricePlanPopular") @set:PropertyName("pricePlanPopular") var pricePlanPopular: Int = 299,
    @get:PropertyName("pricePlanPremium") @set:PropertyName("pricePlanPremium") var pricePlanPremium: Int = 499,
    @get:PropertyName("creditsPlanBasic") @set:PropertyName("creditsPlanBasic") var creditsPlanBasic: Int = 499,
    @get:PropertyName("creditsPlanPopular") @set:PropertyName("creditsPlanPopular") var creditsPlanPopular: Int = 1599,
    @get:PropertyName("creditsPlanPremium") @set:PropertyName("creditsPlanPremium") var creditsPlanPremium: Int = 2999,
    @get:PropertyName("razorpayKeyIdSandbox") @set:PropertyName("razorpayKeyIdSandbox") var razorpayKeyIdSandbox: String = "rzp_test_your_sandbox_key",
    @get:PropertyName("razorpayKeyIdProduction") @set:PropertyName("razorpayKeyIdProduction") var razorpayKeyIdProduction: String = "rzp_live_your_production_key",
    @get:PropertyName("openRouterApiKey") @set:PropertyName("openRouterApiKey") var openRouterApiKey: String = "",
    @get:PropertyName("openRouterModel") @set:PropertyName("openRouterModel") var openRouterModel: String = "google/gemini-2.5-flash-lite:free"
)
