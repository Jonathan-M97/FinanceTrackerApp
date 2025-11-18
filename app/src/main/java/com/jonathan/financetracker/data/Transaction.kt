package com.jonathan.financetracker.data

data class Transaction(
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val type: String = "",
)

val transactions = listOf(
    Transaction("Walmart", 12.34, "Feb 21", "Groceries"),
    Transaction("Target", 12.34, "Feb 21", "Groceries"),
    Transaction("Costco", 12.34, "Feb 21", "Groceries"),
    Transaction("Gas Kroger", 12.34, "Feb 21", "Groceries"),
    Transaction("Kroger", 12.34, "Feb 21", "Groceries"),
    Transaction("Harbor Freight", 12.34, "Feb 21", "Groceries"),
    Transaction("Home Depot", 12.34, "Feb 21", "Groceries"),
    Transaction("Sams Club", 12.34, "Feb 21", "Groceries"),
    Transaction("Hospital", 12.34, "Feb 21", "Groceries"),
    Transaction("Nationwide Childrens", 12.34, "Feb 21", "Groceries"),
    Transaction("Tithing", 12.34, "Feb 21", "Groceries"),
    Transaction("Church", 12.34, "Feb 21", "Groceries"),
    Transaction("Walmart", 12.34, "Feb 21", "Groceries"),
    Transaction("Target", 12.34, "Feb 21", "Groceries"),
    Transaction("Costco", 12.34, "Feb 21", "Groceries"),
    Transaction("Gas Kroger", 12.34, "Feb 21", "Groceries"),
    Transaction("Kroger", 12.34, "Feb 21", "Groceries"),
    Transaction("Harbor Freight", 12.34, "Feb 21", "Groceries"),
    Transaction("Home Depot", 12.34, "Feb 21", "Groceries"),
    Transaction("Sams Club", 12.34, "Feb 21", "Groceries"),
    Transaction("Hospital", 12.34, "Feb 21", "Groceries"),
    Transaction("Nationwide Childrens", 12.34, "Feb 21", "Groceries"),
    Transaction("Tithing", 12.34, "Feb 21", "Groceries"),
    Transaction("Church", 12.34, "Feb 21", "Groceries")
)