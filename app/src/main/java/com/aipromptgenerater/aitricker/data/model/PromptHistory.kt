package com.aipromptgenerater.aitricker.data.model

import com.google.firebase.firestore.PropertyName

data class PromptHistory(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "Website", // "Website" or "App"
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("idea") @set:PropertyName("idea") var idea: String = "",
    @get:PropertyName("techStack") @set:PropertyName("techStack") var techStack: String = "",
    @get:PropertyName("features") @set:PropertyName("features") var features: String = "",
    @get:PropertyName("response") @set:PropertyName("response") var response: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis()
)
