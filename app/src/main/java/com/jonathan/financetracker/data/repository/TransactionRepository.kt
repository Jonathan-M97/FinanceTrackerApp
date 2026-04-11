package com.jonathan.financetracker.data.repository


import com.jonathan.financetracker.data.datasource.TransactionRemoteDataSource
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlyTransactions(
        currentUserIdFlow: Flow<String?>,
        yearMonth: Flow<YearMonth>
    ): Flow<List<Transaction>> =
        combine(currentUserIdFlow, yearMonth) { userId, month ->
            userId to month
        }.flatMapLatest { (userId, month) ->
            if (userId == null) return@flatMapLatest flowOf(emptyList())
            transactionRemoteDataSource.getMonthlyTransactions(userId, month)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlySpentAmount(
        currentUserIdFlow: Flow<String?>,
        yearMonth: Flow<YearMonth>
    ): Flow<Map<String, Double>> =
        combine(currentUserIdFlow, yearMonth) { userId, month ->
            userId to month
        }.flatMapLatest { (userId, month) ->
            if (userId == null) return@flatMapLatest flowOf(emptyMap())
            transactionRemoteDataSource.getMonthlySpentAmount(userId, month)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTotalMonthlySpentAmount(
        currentUserIdFlow: Flow<String?>,
        yearMonth: Flow<YearMonth>
    ): Flow<Double> =
        combine(currentUserIdFlow, yearMonth) { userId, month ->
            userId to month
        }.flatMapLatest { (userId, month) ->
            if (userId == null) return@flatMapLatest flowOf(0.0)
            transactionRemoteDataSource.getTotalMonthlySpentAmount(userId, month)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTotalMonthlyIncomeAmount(
        currentUserIdFlow: Flow<String?>,
        yearMonth: Flow<YearMonth>
    ): Flow<Double> =
        combine(currentUserIdFlow, yearMonth) { userId, month ->
            userId to month
        }.flatMapLatest { (userId, month) ->
            if (userId == null) return@flatMapLatest flowOf(0.0)
            transactionRemoteDataSource.getTotalMonthlyIncomeAmount(userId, month)
        }

}