package com.jonathan.financetracker.data.repository

import android.system.Os
import androidx.core.util.remove
import com.jonathan.financetracker.data.datasource.TransactionRemoteDataSource
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalTime
import javax.inject.Inject
import java.time.YearMonth
import java.time.ZoneOffset


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

//    fun getMonthlyTransactions(currentUserIdFlow: Flow<String?>, yearMonth: Flow<YearMonth>): Flow<List<Transaction>> {
//        return transactionRemoteDataSource.getMonthlyTransactions(currentUserIdFlow, yearMonth)
//    }

//    fun getMonthlyTransactions(userIdFlow: Flow<String>, month: Flow<YearMonth>): Flow<List<Transaction>> {
//        // ... (logic to combine user and month flows) ...
//        return callbackFlow {
//            // ... (Firestore query setup) ...
//
//            val listener = collection.addSnapshotListener { snapshot, error ->
//                // Handle potential errors from Firestore
//                if (error != null) {
//                    // You might want to close the flow with an error
//                    Os.close(error)
//                    return@addSnapshotListener
//                }
//
//                // If the snapshot is valid (even if empty), process it
//                if (snapshot != null) {
//                    val transactions = snapshot.toObjects(Transaction::class.java)
//                    trySend(transactions).isSuccess // This will send an empty list if snapshot is empty
//                }
//            }
//
//            kotlinx.coroutines.channels.awaitClose { listener.remove() }
//        }
//    }

    fun getMonthlyTransactions(
        currentUserIdFlow: Flow<String?>,
        monthFlow: Flow<YearMonth>
    ): Flow<List<Transaction>> =
        combine(currentUserIdFlow, monthFlow) { userId, month ->
            userId to month
        }.flatMapLatest { (userId, month) ->
            if (userId == null) return@flatMapLatest flowOf(emptyList())
            transactionRemoteDataSource.getMonthlyTransactions(userId, month)
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