package com.jonathan.financetracker.data.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import javax.inject.Inject


class TransactionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTransactions(currentUserIdFlow: Flow<String?>): Flow<List<Transaction>> {
        return currentUserIdFlow.flatMapLatest { ownerId ->
            firestore
                .collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, ownerId)
                .orderBy("date", Query.Direction.DESCENDING)
                .dataObjects()
        }
    }

    suspend fun getTransaction(itemId: String): Transaction? {
        return firestore
            .collection(TRANSACTION_COLLECTION)
            .document(itemId)
            .get()
            .await()
            .toObject<Transaction>()
    }

    suspend fun create(transaction: Transaction): String {
        return firestore
            .collection(TRANSACTION_COLLECTION)
            .add(transaction)
            .await()
            .id
    }

    suspend fun update(transaction: Transaction) {
        firestore
            .collection(TRANSACTION_COLLECTION)
            .document(transaction.id!!)
            .set(transaction)
            .await()
    }

    suspend fun delete(transactionId: String) {
        firestore
            .collection(TRANSACTION_COLLECTION)
            .document(transactionId)
            .delete()
            .await()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlyTransactions(userId: String, ym: YearMonth): Flow<List<Transaction>> =
        callbackFlow {
            val ymString = ym.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))

            val listener = firestore.collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, userId)
                .whereEqualTo(YEAR_MONTH_FIELD, ymString)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObjects(Transaction::class.java).orEmpty())
                }

            awaitClose { listener.remove() }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlySpentAmount(userId: String, ym: YearMonth): Flow<Map<String, Double>> =
        callbackFlow {
            val ymString = ym.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))

            val listener = firestore.collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, userId)
                .whereEqualTo(YEAR_MONTH_FIELD, ymString)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val transactions = snapshot?.toObjects(Transaction::class.java).orEmpty()

                    val result: Map<String, Double> = transactions
                        .filter { it.type == "Expense" }
                        .groupBy { it.budgetName }
                        .mapValues { (_, items) -> items.sumOf { it.amount } }

                    trySend(result)
                }

            awaitClose { listener.remove() }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTotalMonthlySpentAmount(userId: String, ym: YearMonth): Flow<Double> =
        callbackFlow {
            val ymString = ym.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))

            val listener = firestore.collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, userId)
                .whereEqualTo(YEAR_MONTH_FIELD, ymString)
                .whereEqualTo("type", "Expense")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TransactionsRepo", "Firestore snapshot error for userId=$userId, ym=$ym", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val transactions = snapshot?.toObjects(Transaction::class.java).orEmpty()
                    val result: Double = transactions.sumOf { it.amount ?: 0.0 }
                    trySend(result)
                }

            awaitClose { listener.remove() }
        }

    companion object {
        private const val OWNER_ID_FIELD = "ownerId"
        private const val YEAR_MONTH_FIELD = "yearMonth"
        private const val TRANSACTION_COLLECTION = "transactions"
    }
}