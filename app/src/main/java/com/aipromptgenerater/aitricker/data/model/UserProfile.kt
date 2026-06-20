package com.aipromptgenerater.aitricker.data.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("displayName") @set:PropertyName("displayName") var displayName: String = "",
    @get:PropertyName("credits") @set:PropertyName("credits") var credits: Int = 15,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("lastReceiptId") @set:PropertyName("lastReceiptId") var lastReceiptId: String = ""
)
