package com.jonathan.financetracker.data

import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.Transaction

fun addTransactionToFirestore(transaction: Transaction) {
    val firebaseApp = Firebase.app
    val db = Firebase.firestore(firebaseApp, "financetracker")
    val currentUser = Firebase.auth.currentUser

    // We need a logged-in user to add data
    if (currentUser != null) {
        // Adding a new document with an auto-generated ID
        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                println("Transaction successfully added!")
            }
            .addOnFailureListener { e ->
                println("Error adding transaction: $e")
            }
    }
}
fun addBudgetToFirestore(budget: Budget) {
    val firebaseApp = Firebase.app
    val db = Firebase.firestore(firebaseApp, "financetracker")
    val currentUser = Firebase.auth.currentUser

    // We need a logged-in user to add data
    if (currentUser != null) {
        // Adding a new document with an auto-generated ID
        db.collection("budgets")
            .add(budget)
            .addOnSuccessListener {
                println("Transaction successfully added!")
            }
            .addOnFailureListener { e ->
                println("Error adding transaction: $e")
            }
    }
}