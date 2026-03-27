package com.jonathan.financetracker.ui.Transactions

import androidx.lifecycle.viewModelScope
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository
) : MainViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val transactions: StateFlow<List<Transaction>> =
        transactionRepository.getMonthlyTransactions(
            currentUserIdFlow = authRepository.currentUserIdFlow,
            yearMonth = selectedMonth
        ).onEach {
            _isLoading.value = false
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun goToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun goToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun canGoToNextMonth(): Boolean {
        return _selectedMonth.value.isBefore(YearMonth.now())
    }
}
