package com.jonathan.financetracker.data

import com.jonathan.financetracker.ui.Transactions.SortDirection
import com.jonathan.financetracker.ui.Transactions.SortField
import com.jonathan.financetracker.ui.Transactions.TransactionFilterState
import com.jonathan.financetracker.ui.Transactions.TransactionTypeFilter
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Holds the transaction-screen filter and sort selections at the
 * Activity-retained scope so they survive TransactionsViewModel
 * recreation while still resetting when the user truly relaunches
 * the app.
 *
 * The Transactions tab's ViewModel can be torn down and re-created
 * whenever the user navigates to AddTransaction and the nav graph
 * pushes a fresh MainTabsRoute entry on return. Without a shared
 * holder, every save/delete would reset the user's filter/sort
 * selections back to defaults.
 *
 * @ActivityRetainedScoped means the same instance is shared across
 * configuration changes (rotation, theme) and ViewModel re-creations
 * within a single Activity, but a brand-new instance is created when
 * the Activity is finished and re-launched (cold start from launcher,
 * post-process-death restart, etc.). That gives the user a "fresh"
 * filter state on every real app launch.
 */
@ActivityRetainedScoped
class SharedTransactionFilterState @Inject constructor() {

    private val _filterState = MutableStateFlow(TransactionFilterState())
    val filterState: StateFlow<TransactionFilterState> = _filterState.asStateFlow()

    fun setTypeFilter(filter: TransactionTypeFilter) {
        _filterState.value = _filterState.value.copy(typeFilter = filter)
    }

    fun toggleCategory(category: String) {
        val current = _filterState.value.selectedCategories
        _filterState.value = _filterState.value.copy(
            selectedCategories = if (category in current) current - category else current + category
        )
    }

    fun clearCategories() {
        if (_filterState.value.selectedCategories.isEmpty()) return
        _filterState.value = _filterState.value.copy(selectedCategories = emptySet())
    }

    fun toggleSort(field: SortField) {
        val current = _filterState.value
        _filterState.value = if (current.sortField == field) {
            current.copy(
                sortDirection = if (current.sortDirection == SortDirection.DESCENDING)
                    SortDirection.ASCENDING
                else
                    SortDirection.DESCENDING
            )
        } else {
            current.copy(sortField = field, sortDirection = SortDirection.DESCENDING)
        }
    }
}
