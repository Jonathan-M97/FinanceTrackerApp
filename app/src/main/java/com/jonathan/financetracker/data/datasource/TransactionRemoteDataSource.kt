package com.jonathan.financetracker.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
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

//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun getMonthlySpentAmount(currentUserIdFlow: Flow<String?>, monthsAgo: YearMonth): Flow<Map<String, Double>> {
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.MONTH, -monthsAgo)
//        calendar.set(Calendar.DAY_OF_MONTH, 1)
//        val firstDayOfMonth = calendar.time
//
//        calendar.add(Calendar.MONTH, 1)
//        calendar.add(Calendar.DAY_OF_MONTH, -1)
//        val lastDayOfMonth = calendar.time
//
//        val transactions = firestore
//            .collection(TRANSACTION_COLLECTION)
//            .whereEqualTo(OWNER_ID_FIELD, currentUserIdFlow)
//            .whereGreaterThanOrEqualTo("date", firstDayOfMonth)
//            .whereLessThanOrEqualTo("date", lastDayOfMonth)
//            .get()
//            .await()
//            .toObjects<Transaction>()
//
//        return transactions
//            .groupBy { it.budgetName ?: "" }
//            .mapValues { (_, transactions) ->
//                transactions.sumOf { it.amount ?: 0.0 }
//            }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlySpentAmount(currentUserIdFlow: Flow<String?>, yearMonthFlow: Flow<YearMonth>): Flow<Map<String, Double>> {
        // 1. Combine the latest user ID and the latest selected month
        return currentUserIdFlow.combine(yearMonthFlow) { ownerId, yearMonth ->
            // Create a pair of the ownerId and the calculated date range
            val startOfMonth = yearMonth.atDay(1)
            val endOfMonth = yearMonth.atEndOfMonth()
            Pair(ownerId, Pair(startOfMonth, endOfMonth))
        }.flatMapLatest { (ownerId, dateRange) -> // 2. Use flatMapLatest for efficient querying
            val (start, end) = dateRange

            // 3. Return a new data flow from Firestore
            firestore
                .collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, ownerId)
                // Use the start and end of the month for the query
                .whereGreaterThanOrEqualTo("date", java.util.Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
                .whereLessThanOrEqualTo("date", java.util.Date.from(end.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
                .dataObjects<Transaction>() // 4. Use dataObjects for a real-time stream
                .map { transactions ->
                    // 5. Group the results and sum the amounts
                    transactions
                        .groupBy { it.budgetName ?: "" }
                        .mapValues { (_, groupedTransactions) ->
                            groupedTransactions.sumOf { it.amount ?: 0.0 }
                        }
                }
        }
    }


//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun getMonthlyTransactions(currentUserIdFlow: Flow<String?>, yearMonthFlow: Flow<YearMonth>): Flow<List<Transaction>> {
//        return currentUserIdFlow.combine(yearMonthFlow) { ownerId, yearMonth ->
//            val startOfMonth = yearMonth.atDay(1)
//            val endOfMonth = yearMonth.atEndOfMonth()
//            Pair(ownerId, Pair(startOfMonth, endOfMonth))
//        }.flatMapLatest { (ownerId, dateRange) ->
//            val (start, end) = dateRange
//
//            firestore
//                .collection(TRANSACTION_COLLECTION)
//                .whereEqualTo(OWNER_ID_FIELD, ownerId)
//                .whereGreaterThanOrEqualTo("date", java.util.Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
//                .whereLessThanOrEqualTo("date", java.util.Date.from(end.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
//                .orderBy("date", Query.Direction.DESCENDING)
//                .dataObjects<Transaction>()
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlyTransactions(userId: String, ym: YearMonth): Flow<List<Transaction>> =
        callbackFlow {
            val startOfMonth = ym.atDay(1)
            val endOfMonth = ym.atEndOfMonth()


            val startDate = java.util.Date.from(startOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
            val endDate = java.util.Date.from(endOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())

            val listener = firestore.collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING) // It's good practice to order the results
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
    fun getTotalMonthlySpentAmount(currentUserIdFlow: Flow<String?>, yearMonthFlow: Flow<YearMonth>): Flow<Double> {
        return currentUserIdFlow.combine(yearMonthFlow) { ownerId, yearMonth ->
            val startOfMonth = yearMonth.atDay(1)
            val endOfMonth = yearMonth.atEndOfMonth()
            Pair(ownerId, Pair(startOfMonth, endOfMonth))
        }.flatMapLatest { (ownerId, dateRange) ->
            val (start, end) = dateRange

            firestore
                .collection(TRANSACTION_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, ownerId)
                .whereGreaterThanOrEqualTo(
                    "date",
                    java.util.Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                )
                .whereLessThanOrEqualTo(
                    "date",
                    java.util.Date.from(end.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                )
                .dataObjects<Transaction>() // Use dataObjects() for a real-time stream of transactions
                .map { transactions ->
                    // 6. Map the list of transactions to the sum of their amounts
                    transactions.sumOf { it.amount ?: 0.0 }
                }
        }
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