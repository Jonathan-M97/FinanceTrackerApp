package com.jonathan.financetracker.data.repository

import com.jonathan.financetracker.data.datasource.BudgetRemoteDataSource
import com.jonathan.financetracker.data.model.Budget
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val budgetRemoteDataSource: BudgetRemoteDataSource
){
    fun getBudgets(currentUserIdFlow: Flow<String?>): Flow<List<Budget>> {
        return budgetRemoteDataSource.getBudgets(currentUserIdFlow)
    }

    suspend fun getBudget(itemId: String): Budget? {
        return budgetRemoteDataSource.getBudget(itemId)
    }

    suspend fun create(budget: Budget): String {
        return budgetRemoteDataSource.create(budget)
    }

    suspend fun update(budget: Budget) {
        budgetRemoteDataSource.update(budget)
    }

    suspend fun delete(budgetID: String) {
        budgetRemoteDataSource.delete(budgetID)

    }

}