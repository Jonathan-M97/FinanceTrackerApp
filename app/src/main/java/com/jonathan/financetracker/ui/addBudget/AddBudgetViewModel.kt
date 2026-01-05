package com.jonathan.financetracker.ui.addBudget

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddBudgetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val budgetRepository: BudgetRepository
) : MainViewModel() {

    private val _navigateDashboard = MutableStateFlow(false)
    val navigateDashboard: StateFlow<Boolean>
        get() = _navigateDashboard.asStateFlow()

    private val _navigateBudget = MutableStateFlow(false)
    val navigateBudget: StateFlow<Boolean> = _navigateBudget.asStateFlow()

    private val addBudgetRouteArgs = savedStateHandle.toRoute<AddBudgetRoute>()
    private val itemId: String = addBudgetRouteArgs.itemId

    private val _budgetItem = MutableStateFlow<Budget?>(null)
    val budgetItem: StateFlow<Budget?>
        get() = _budgetItem.asStateFlow()

    fun loadItem() {
        launchCatching {
            if (itemId.isBlank()) {
                _budgetItem.value = Budget()
            } else {
                _budgetItem.value = budgetRepository.getBudget(itemId)
            }
        }
    }

    fun saveItem(
        item: Budget,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        val ownerId = authRepository.currentUser?.uid

        if (ownerId.isNullOrBlank()) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_missing_owner_id))
                return
        }

        if (item.category.isBlank()) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_missing_category))
            return
        }

        launchCatching {
            val newItem = item.copy(ownerId = ownerId)
            if (itemId.isBlank()) {
                budgetRepository.create(newItem)
            } else {
                budgetRepository.update(newItem)
            }

            _navigateBudget.value = true
        }

    }

    /**
     * Deletes the current budget item from the repository.
     */
    fun deleteItem(
        item: Budget,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        // We only proceed if there's a valid item ID, meaning the item is not new.
        if (itemId.isNotBlank()) {
            item.id?.let { id -> // Use a safe call ?.let to ensure the id is not null
                launchCatching {
                    budgetRepository.delete(id) // Use the non-null 'id' here
                    // Navigate back to the budget list after deletion is successful.
                    _navigateBudget.value = true
                }
            } ?: showErrorSnackbar(ErrorMessage.IdError(R.string.error_missing_id)) // Optional: Show an error if the id is null
        }
    }


}