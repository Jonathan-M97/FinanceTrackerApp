package com.jonathan.financetracker.data.model

import com.google.firebase.firestore.DocumentId

data class PlaidCategoryMapping(
    @DocumentId val id: String? = null,
    val ownerId: String = "",
    val mappings: Map<String, BudgetMapping> = emptyMap()
)

data class BudgetMapping(
    val budgetId: String = "",
    val budgetName: String = ""
)

object PlaidCategories {
    val TOP_LEVEL = listOf(
        "Bank Fees",
        "Cash Advance",
        "Community",
        "Food and Drink",
        "Healthcare",
        "Interest",
        "Payment",
        "Recreation",
        "Service",
        "Shops",
        "Tax",
        "Transfer",
        "Travel"
    )
}
