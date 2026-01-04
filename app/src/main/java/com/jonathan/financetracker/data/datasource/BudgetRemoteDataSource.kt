package com.jonathan.financetracker.data.datasource


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.jonathan.financetracker.data.model.Budget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BudgetRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getBudgets(currentUserIdFlow: Flow<String?>): Flow<List<Budget>> {
        return currentUserIdFlow.flatMapLatest { ownerId ->
            firestore
                .collection(BUDGET_COLLECTION)
                .whereEqualTo(OWNER_ID_FIELD, ownerId)
                .dataObjects()
        }
    }

    suspend fun getBudget(itemId: String): Budget? {
        return firestore
            .collection(BUDGET_COLLECTION)
            .document(itemId)
            .get()
            .await()
            .toObject()
    }

    suspend fun create(budget: Budget): String {
        return firestore
            .collection(BUDGET_COLLECTION)
            .add(budget)
            .await()
            .id
    }

//    suspend fun update(budget: Budget) {
//        firestore
//            .collection(BUDGET_COLLECTION)
//            .document(budget.id)
//            .set(budget)
//            .await()
//    }


    companion object {
        private const val OWNER_ID_FIELD = "ownerId"
        private const val BUDGET_COLLECTION = "budgets"
    }
}