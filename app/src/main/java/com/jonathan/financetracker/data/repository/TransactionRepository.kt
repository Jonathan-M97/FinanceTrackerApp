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
}