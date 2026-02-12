package com.jonathan.financetracker.data.repository

import com.jonathan.financetracker.data.datasource.TransactionRemoteDataSource
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import java.time.YearMonth

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

    fun getMonthlySpentAmount(currentUserIdFlow: Flow<String?>, yearMonth: Flow<YearMonth>): Flow<Map<String, Double>> {
        return transactionRemoteDataSource.getMonthlySpentAmount(currentUserIdFlow, yearMonth)
    }

    fun getMonthlyTransactions(currentUserIdFlow: Flow<String?>, yearMonth: Flow<YearMonth>): Flow<List<Transaction>> {
        return transactionRemoteDataSource.getMonthlyTransactions(currentUserIdFlow, yearMonth)
    }

    fun getTotalMonthlySpentAmount(currentUserIdFlow: Flow<String?>, yearMonth: Flow<YearMonth>): Flow<Double> {
        return transactionRemoteDataSource.getTotalMonthlySpentAmount(currentUserIdFlow, yearMonth)
    }

    fun getCurrentMonthlySpentAmount(currentUserIdFlow: Flow<String?>): Flow<Map<String, Double>> {
        return transactionRemoteDataSource.getCurrentMonthlySpentAmount(currentUserIdFlow)
    }


    suspend fun getTotalCurrentMonthlySpentAmount(ownerId: String): Double {
        return transactionRemoteDataSource.getTotalCurrentMonthlySpentAmount(ownerId)
    }
}