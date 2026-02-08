package com.jonathan.financetracker.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
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

    suspend fun getMonthlySpentAmount(ownerId: String, monthsAgo: Int): Map<String, Double> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val lastDayOfMonth = calendar.time

        val transactions = firestore
            .collection(TRANSACTION_COLLECTION)
            .whereEqualTo(OWNER_ID_FIELD, ownerId)
            .whereGreaterThanOrEqualTo("date", firstDayOfMonth)
            .whereLessThanOrEqualTo("date", lastDayOfMonth)
            .get()
            .await()
            .toObjects<Transaction>()

        return transactions
            .groupBy { it.budgetName ?: "" }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount ?: 0.0 }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlyTransactions(currentUserIdFlow: Flow<String?>, monthsAgo: Int): Flow<List<Transaction>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val firstDayOfMonth = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val lastDayOfMonth = calendar.time

        return currentUserIdFlow.flatMapLatest { ownerId ->
                firestore
                    .collection(TRANSACTION_COLLECTION)
                    .whereEqualTo(OWNER_ID_FIELD, ownerId)
                    .whereGreaterThanOrEqualTo("date", firstDayOfMonth)
                    .whereLessThanOrEqualTo("date", lastDayOfMonth)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .dataObjects()
        }
    }

    suspend fun getTotalMonthlySpentAmount(ownerId: String, monthsAgo: Int): Double {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val lastDayOfMonth = calendar.time

        val transactions = firestore
            .collection(TRANSACTION_COLLECTION)
            .whereEqualTo(OWNER_ID_FIELD, ownerId)
            .whereGreaterThanOrEqualTo("date", firstDayOfMonth)
            .whereLessThanOrEqualTo("date", lastDayOfMonth)
            .get()
            .await()
            .toObjects<Transaction>()

        return transactions.sumOf { it.amount ?: 0.0 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCurrentMonthlySpentAmount(currentUserIdFlow: Flow<String?>): Flow<Map<String, Double>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val firstDayOfMonth = calendar.time

        return currentUserIdFlow.flatMapLatest { ownerId ->
            firestore
                .collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, ownerId)
                .whereGreaterThanOrEqualTo("date", firstDayOfMonth)
                .dataObjects<Transaction>()
                .map { transactions ->
                    transactions
                        .groupBy { it.budgetName ?: "" }
                        .mapValues { (_, groupedTransactions) ->
                            groupedTransactions.sumOf { it.amount ?: 0.0 }
                        }
                }
        }
    }

    suspend fun getTotalCurrentMonthlySpentAmount(ownerId: String): Double {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time

        val transactions = firestore
            .collection(TRANSACTION_COLLECTION)
            .whereEqualTo(OWNER_ID_FIELD, ownerId)
            .whereGreaterThanOrEqualTo("date", firstDayOfMonth)
            .get()
            .await()
            .toObjects<Transaction>()

        return transactions.sumOf { it.amount ?: 0.0 }

    }


    companion object {
        private const val OWNER_ID_FIELD = "ownerId"
        private const val TRANSACTION_COLLECTION = "transactions"
    }
}