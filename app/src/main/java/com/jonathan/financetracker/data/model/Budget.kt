package com.jonathan.financetracker.data.model

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId val id: String? = null,
    val category: String = "",
    val amount: Double = 0.0,
    val ownerId: String = "",
)