package com.jonathan.financetracker.data.datasource

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.jonathan.financetracker.data.model.LinkedAccount
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlaidRemoteDataSource @Inject constructor(
    private val functions: FirebaseFunctions
) {
    suspend fun createLinkToken(): String {
        val data = callFunction("createLinkToken")
        return data["linkToken"] as? String
            ?: throw Exception("Invalid response: missing link token")
    }

    suspend fun exchangePublicToken(
        publicToken: String,
        institutionName: String,
        institutionId: String
    ): String {
        val data = callFunction(
            "exchangePublicToken",
            hashMapOf(
                "publicToken" to publicToken,
                "institutionName" to institutionName,
                "institutionId" to institutionId
            )
        )
        return data["itemId"] as? String
            ?: throw Exception("Invalid response: missing item ID")
    }

    suspend fun syncTransactions(): Int {
        val data = callFunction("syncTransactions")
        return (data["added"] as? Number)?.toInt() ?: 0
    }

    suspend fun getLinkedAccounts(): List<LinkedAccount> {
        val data = callFunction("getLinkedAccounts")
        val accounts = data["accounts"] as? List<*> ?: emptyList<Any>()

        return accounts.mapNotNull { item ->
            val account = item as? Map<*, *> ?: return@mapNotNull null
            LinkedAccount(
                itemId = account["itemId"] as? String ?: "",
                institutionName = account["institutionName"] as? String ?: "Unknown Bank"
            )
        }
    }

    suspend fun unlinkAccount(itemId: String) {
        callFunction("unlinkAccount", hashMapOf("itemId" to itemId))
    }

    suspend fun purgeSyncedTransactions(): Int {
        val data = callFunction("purgeSyncedTransactions")
        return (data["deleted"] as? Number)?.toInt() ?: 0
    }

    /**
     * Calls a Firebase Cloud Function and returns the result data as a Map.
     * Extracts the error message from FirebaseFunctionsException so that
     * Plaid error codes (returned by our Cloud Functions) propagate to the ViewModel.
     */
    private suspend fun callFunction(
        name: String,
        data: HashMap<String, Any>? = null
    ): Map<String, Any> {
        try {
            val callable = functions.getHttpsCallable(name)
            val result = if (data != null) {
                callable.call(data).await()
            } else {
                callable.call().await()
            }

            @Suppress("UNCHECKED_CAST")
            return result.getData() as? Map<String, Any>
                ?: throw Exception("Invalid response from $name")
        } catch (e: FirebaseFunctionsException) {
            // Surface specific messages for known error codes
            val message = when (e.code) {
                FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED ->
                    "Too many requests. Please wait a few minutes before trying again."
                FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                    "You must be signed in to use this feature."
                else -> e.message ?: "Cloud function $name failed"
            }
            throw Exception(message, e)
        }
    }
}
