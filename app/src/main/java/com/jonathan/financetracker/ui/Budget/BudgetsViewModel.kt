package com.jonathan.financetracker.ui.Budget

import androidx.lifecycle.viewModelScope
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.SharedMonthState
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.BudgetRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import java.time.YearMonth


@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val sharedMonthState: SharedMonthState,
) : MainViewModel() {

    val selectedMonth: StateFlow<YearMonth> = sharedMonthState.selectedMonth

    // State for if the user is loaded
    private val _isLoadingUser = MutableStateFlow(true)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    // State for if the user is not logged in
    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean>
        get() = _isAnonymous.asStateFlow()

    val spentAmounts: StateFlow<Map<String, Double>> =
        // Pass both the user ID flow and the selected month flow
        transactionRepository.getMonthlySpentAmount(
            currentUserIdFlow = authRepository.currentUserIdFlow,
            yearMonth = selectedMonth
        ).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    private val _isLoadingData = MutableStateFlow(true)
    val isLoadingData: StateFlow<Boolean> = _isLoadingData.asStateFlow()

    val budgets = budgetRepository.getBudgets(authRepository.currentUserIdFlow)
        .onEach { _isLoadingData.value = false }

    fun goToNextMonth() = sharedMonthState.goToNextMonth()
    fun goToPreviousMonth() = sharedMonthState.goToPreviousMonth()
    fun canGoToNextMonth() = sharedMonthState.canGoToNextMonth()
    fun loadCurrentUser() {
        launchCatching {
            if (authRepository.currentUser == null) {
                authRepository.createGuestAccount()
            }

            _isAnonymous.value = authRepository.currentUser?.isAnonymous == true
            _isLoadingUser.value = false
        }
    }
}