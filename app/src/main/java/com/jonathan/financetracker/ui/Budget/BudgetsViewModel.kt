package com.jonathan.financetracker.ui.Budget

import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.BudgetRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val budgetsRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : MainViewModel() {

    private val _isLoadingUser = MutableStateFlow(true)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean>
        get() = _isAnonymous.asStateFlow()

    private val _spentAmounts = MutableStateFlow<Map<String, Double>>(emptyMap())
    val spentAmounts: StateFlow<Map<String, Double>> = _spentAmounts.asStateFlow()

    val _totalMonthlySpentAmount = MutableStateFlow(0.0)
    val totalMonthlySpentAmount: StateFlow<Double> = _totalMonthlySpentAmount.asStateFlow()



    val budgets = budgetsRepository.getBudgets(authRepository.currentUserIdFlow)

    fun loadCurrentUser() {
        launchCatching {
            if (authRepository.currentUser == null) {
                authRepository.createGuestAccount()
            }

            authRepository.currentUser?.uid?.let { loadSpentAmounts(it) }
            _isAnonymous.value = authRepository.currentUser?.isAnonymous == true
            _isLoadingUser.value = false
        }
    }

    private fun loadSpentAmounts(userId: String) {
        launchCatching {
            _spentAmounts.value = transactionRepository.getMonthlySpentAmount(userId)
            _totalMonthlySpentAmount.value = transactionRepository.getTotalMonthlySpentAmount(userId)
        }
    }
}