package com.jonathan.financetracker.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.jonathan.financetracker.data.model.PlaidCategoryMapping
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlaidCategoryMappingDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getMapping(ownerId: String): PlaidCategoryMapping? {
        val snapshot = firestore
            .collection(COLLECTION)
            .whereEqualTo(OWNER_ID_FIELD, ownerId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<PlaidCategoryMapping>()
    }

    suspend fun saveMapping(mapping: PlaidCategoryMapping) {
        val existing = mapping.id
        if (existing != null) {
            firestore.collection(COLLECTION)
                .document(existing)
                .set(mapping)
                .await()
        } else {
            firestore.collection(COLLECTION)
                .add(mapping)
                .await()
        }
    }

    companion object {
        private const val COLLECTION = "plaid_category_mappings"
        private const val OWNER_ID_FIELD = "ownerId"
    }
}
