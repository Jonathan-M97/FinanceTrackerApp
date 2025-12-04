package com.jonathan.financetracker.data.model

data class Transaction(
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val type: String = "",
)