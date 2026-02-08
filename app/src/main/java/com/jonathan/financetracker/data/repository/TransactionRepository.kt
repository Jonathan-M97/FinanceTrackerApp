package com.jonathan.financetracker.data.repository

import com.jonathan.financetracker.data.datasource.TransactionRemoteDataSource
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource
) {
    fun getTransactions(currentUserIdFlow: Flow<String?>): Flow<List<Transaction>> {
        return transactionRemoteDataSource.getTransactions(currentUserIdFlow)
    }

    suspend fun getTransaction(itemId: String): Transaction? {
        return transactionRemoteDataSource.getTransaction(itemId)
    }

    suspend fun create(transaction: Transaction): String {
        return transactionRemoteDataSource.create(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionRemoteDataSource.update(transaction)
    }

    suspend fun delete(transactionId: String) {
        transactionRemoteDataSource.delete(transactionId)
    }

    suspend fun getMonthlySpentAmount(ownerId: String, monthsAgo: Int): Map<String, Double> {
        return transactionRemoteDataSource.getMonthlySpentAmount(ownerId, monthsAgo)
    }


    fun getMonthlyTransactions(currentUserIdFlow: Flow<String?>, monthsAgo: Int): Flow<List<Transaction>> {
        return transactionRemoteDataSource.getMonthlyTransactions(currentUserIdFlow, monthsAgo)
    }

    suspend fun getTotalMonthlySpentAmount(ownerId: String, monthsAgo: Int): Double {
        return transactionRemoteDataSource.getTotalMonthlySpentAmount(ownerId, monthsAgo)
    }

    fun getCurrentMonthlySpentAmount(currentUserIdFlow: Flow<String?>): Flow<Map<String, Double>> {
        return transactionRemoteDataSource.getCurrentMonthlySpentAmount(currentUserIdFlow)
    }


    suspend fun getTotalCurrentMonthlySpentAmount(ownerId: String): Double {
        return transactionRemoteDataSource.getTotalCurrentMonthlySpentAmount(ownerId)
    }
}