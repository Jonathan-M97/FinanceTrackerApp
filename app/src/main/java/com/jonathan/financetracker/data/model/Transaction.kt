package com.jonathan.financetracker.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp

data class Transaction(
    @DocumentId val id: String? = null,
    val description: String = "",
    val amount: Double = 0.0,
    val date: Timestamp = Timestamp.now(),
    val type: String = "Expense",
    val methodOfPayment: String = "Credit Card",
    val budgetName: String = "",
    val budgetId: String = "",
    val ownerId: String = "",
    val yearMonth: String = "",
    val isManuallyCreated: Boolean = true,
    val plaidTransactionId: String? = null,
    val plaidCategory: String? = null,
    val plaidItemId: String? = null
)

