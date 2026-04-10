package com.jonathan.financetracker.ui.addBudget

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.BudgetMapping
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.model.PlaidCategories
import com.jonathan.financetracker.data.model.PlaidCategoryMapping
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.BudgetRepository
import com.jonathan.financetracker.data.repository.PlaidCategoryMappingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddBudgetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val budgetRepository: BudgetRepository,
    private val mappingRepository: PlaidCategoryMappingRepository
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

    private val _linkedCategories = MutableStateFlow<Set<String>>(emptySet())
    val linkedCategories: StateFlow<Set<String>> = _linkedCategories.asStateFlow()

    val plaidCategories = PlaidCategories.TOP_LEVEL

    private var existingMapping: PlaidCategoryMapping? = null

    fun loadItem() {
        launchCatching {
            if (itemId.isBlank()) {
                _budgetItem.value = Budget()
            } else {
                _budgetItem.value = budgetRepository.getBudget(itemId)
            }

            // Load category mappings for this budget
            if (itemId.isNotBlank()) {
                val ownerId = authRepository.currentUser?.uid ?: return@launchCatching
                val mapping = mappingRepository.getMapping(ownerId)
                existingMapping = mapping
                if (mapping != null) {
                    _linkedCategories.value = mapping.mappings
                        .filter { it.value.budgetId == itemId }
                        .keys
                }
            }
        }
    }

    fun togglePlaidCategory(plaidCategory: String) {
        val current = _linkedCategories.value.toMutableSet()
        if (current.contains(plaidCategory)) {
            current.remove(plaidCategory)
        } else {
            current.add(plaidCategory)
        }
        _linkedCategories.value = current
    }

    private suspend fun saveMappings(budgetId: String, budgetName: String) {
        val ownerId = authRepository.currentUser?.uid ?: return
        val currentMappings = (existingMapping?.mappings ?: emptyMap()).toMutableMap()

        // Remove any existing mappings that point to this budget
        currentMappings.entries.removeAll { it.value.budgetId == budgetId }

        // Add new mappings for selected categories
        for (category in _linkedCategories.value) {
            currentMappings[category] = BudgetMapping(
                budgetId = budgetId,
                budgetName = budgetName
            )
        }

        val mapping = PlaidCategoryMapping(
            id = existingMapping?.id,
            ownerId = ownerId,
            mappings = currentMappings
        )
        mappingRepository.saveMapping(mapping)
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

        if (item.category.length > 50) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_category_too_long))
            return
        }

        if (item.amount <= 0.0) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_invalid_amount))
            return
        }

        if (item.amount > 999_999.99) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_amount_too_large))
            return
        }

        launchCatching {
            val newItem = item.copy(ownerId = ownerId)
            val savedId: String
            if (itemId.isBlank()) {
                savedId = budgetRepository.create(newItem)
            } else {
                budgetRepository.update(newItem)
                savedId = itemId
            }

            // Save Plaid category mappings if any are selected
            if (_linkedCategories.value.isNotEmpty() || existingMapping != null) {
                saveMappings(savedId, newItem.category)
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