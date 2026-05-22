package com.jonathan.financetracker.ui.Transactions

import androidx.lifecycle.viewModelScope
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.SharedMonthState
import com.jonathan.financetracker.data.SharedTransactionFilterState
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

enum class TransactionTypeFilter { ALL, INCOME, EXPENSE }
enum class SortField { DATE, AMOUNT }
enum class SortDirection { ASCENDING, DESCENDING }

data class TransactionFilterState(
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val selectedCategories: Set<String> = emptySet(),
    val sortField: SortField = SortField.DATE,
    val sortDirection: SortDirection = SortDirection.DESCENDING
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val sharedMonthState: SharedMonthState,
    private val sharedFilterState: SharedTransactionFilterState,
) : MainViewModel() {

    val selectedMonth: StateFlow<YearMonth> = sharedMonthState.selectedMonth

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filterState: StateFlow<TransactionFilterState> = sharedFilterState.filterState

    private val rawTransactions: StateFlow<List<Transaction>> =
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

    val availableCategories: StateFlow<List<String>> =
        rawTransactions.map { transactions ->
            transactions.map { it.budgetName }.distinct().sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<Transaction>> =
        combine(rawTransactions, filterState) { transactions, filter ->
            var result = transactions

            // Type filter
            result = when (filter.typeFilter) {
                TransactionTypeFilter.ALL -> result
                TransactionTypeFilter.INCOME -> result.filter { it.type == "Income" }
                TransactionTypeFilter.EXPENSE -> result.filter { it.type == "Expense" }
            }

            // Category filter
            if (filter.selectedCategories.isNotEmpty()) {
                result = result.filter { it.budgetName in filter.selectedCategories }
            }

            // Sort
            result = when (filter.sortField) {
                SortField.DATE -> when (filter.sortDirection) {
                    SortDirection.DESCENDING -> result.sortedByDescending { it.date }
                    SortDirection.ASCENDING -> result.sortedBy { it.date }
                }
                SortField.AMOUNT -> when (filter.sortDirection) {
                    SortDirection.DESCENDING -> result.sortedByDescending { it.amount }
                    SortDirection.ASCENDING -> result.sortedBy { it.amount }
                }
            }

            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Clear category filter when the user navigates to a different
        // month, since categories are scoped per-month. drop(1) skips
        // the StateFlow's initial replay so we don't clobber any
        // categories the user had selected before this ViewModel was
        // re-created (e.g. after returning from AddTransaction).
        selectedMonth.drop(1).onEach {
            sharedFilterState.clearCategories()
        }.launchIn(viewModelScope)
    }

    fun setTypeFilter(filter: TransactionTypeFilter) = sharedFilterState.setTypeFilter(filter)

    fun toggleCategory(category: String) = sharedFilterState.toggleCategory(category)

    fun clearCategoryFilter() = sharedFilterState.clearCategories()

    fun toggleSort(field: SortField) = sharedFilterState.toggleSort(field)

    fun goToNextMonth() = sharedMonthState.goToNextMonth()
    fun goToPreviousMonth() = sharedMonthState.goToPreviousMonth()
    fun canGoToNextMonth() = sharedMonthState.canGoToNextMonth()
}
