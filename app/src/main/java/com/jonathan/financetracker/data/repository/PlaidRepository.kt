package com.jonathan.financetracker.data.repository

import com.jonathan.financetracker.data.datasource.PlaidRemoteDataSource
import com.jonathan.financetracker.data.model.LinkedAccount
import javax.inject.Inject

class PlaidRepository @Inject constructor(
    private val plaidRemoteDataSource: PlaidRemoteDataSource
) {
    suspend fun createLinkToken(): String {
        return plaidRemoteDataSource.createLinkToken()
    }

    suspend fun exchangePublicToken(
        publicToken: String,
        institutionName: String,
        institutionId: String
    ): String {
        return plaidRemoteDataSource.exchangePublicToken(
            publicToken, institutionName, institutionId
        )
    }

    suspend fun syncTransactions(): Int {
        return plaidRemoteDataSource.syncTransactions()
    }

    suspend fun getLinkedAccounts(): List<LinkedAccount> {
        return plaidRemoteDataSource.getLinkedAccounts()
    }

    suspend fun unlinkAccount(itemId: String) {
        plaidRemoteDataSource.unlinkAccount(itemId)
    }
}
