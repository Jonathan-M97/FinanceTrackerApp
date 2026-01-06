package com.jonathan.financetracker.data.model

import com.google.firebase.firestore.DocumentId

data class Transaction(
    @DocumentId val id: String? = null,
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val type: String = "",
    val ownerId: String = "",
)