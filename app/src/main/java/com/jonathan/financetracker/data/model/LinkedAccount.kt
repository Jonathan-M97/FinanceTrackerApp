package com.jonathan.financetracker.data.model

data class LinkedAccount(
    val itemId: String = "",
    val institutionName: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)
