package com.jonathan.financetracker.ui.Transactions

import androidx.lifecycle.viewModelScope
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.SharedMonthState
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
) : MainViewModel() {

    val selectedMonth: StateFlow<YearMonth> = sharedMonthState.selectedMonth

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filterState = MutableStateFlow(TransactionFilterState())
    val filterState: StateFlow<TransactionFilterState> = _filterState.asStateFlow()

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
        combine(rawTransactions, _filterState) { transactions, filter ->
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
        selectedMonth.onEach {
            _filterState.value = _filterState.value.copy(selectedCategories = emptySet())
        }.launchIn(viewModelScope)
    }

    fun setTypeFilter(filter: TransactionTypeFilter) {
        _filterState.value = _filterState.value.copy(typeFilter = filter)
    }

    fun toggleCategory(category: String) {
        val current = _filterState.value.selectedCategories
        _filterState.value = _filterState.value.copy(
            selectedCategories = if (category in current) current - category else current + category
        )
    }

    fun clearCategoryFilter() {
        _filterState.value = _filterState.value.copy(selectedCategories = emptySet())
    }

    fun toggleSort(field: SortField) {
        val current = _filterState.value
        _filterState.value = if (current.sortField == field) {
            current.copy(
                sortDirection = if (current.sortDirection == SortDirection.DESCENDING)
                    SortDirection.ASCENDING else SortDirection.DESCENDING
            )
        } else {
            current.copy(sortField = field, sortDirection = SortDirection.DESCENDING)
        }
    }

    fun goToNextMonth() = sharedMonthState.goToNextMonth()
    fun goToPreviousMonth() = sharedMonthState.goToPreviousMonth()
    fun canGoToNextMonth() = sharedMonthState.canGoToNextMonth()
}
