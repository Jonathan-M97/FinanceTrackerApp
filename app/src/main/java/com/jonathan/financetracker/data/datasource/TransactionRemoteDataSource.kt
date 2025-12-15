package com.jonathan.financetracker.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.jonathan.financetracker.data.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
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
                .dataObjects()
        }
    }
    companion object {
        private const val OWNER_ID_FIELD = "ownerId"
        private const val TRANSACTION_COLLECTION = "transactions"
    }
}