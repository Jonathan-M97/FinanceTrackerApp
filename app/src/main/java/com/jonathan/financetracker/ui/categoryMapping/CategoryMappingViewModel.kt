package com.jonathan.financetracker.ui.categoryMapping

import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.model.BudgetMapping
import com.jonathan.financetracker.data.model.PlaidCategories
import com.jonathan.financetracker.data.model.PlaidCategoryMapping
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.BudgetRepository
import com.jonathan.financetracker.data.repository.PlaidCategoryMappingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class CategoryMappingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val budgetRepository: BudgetRepository,
    private val mappingRepository: PlaidCategoryMappingRepository
) : MainViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    private val _mappings = MutableStateFlow<Map<String, BudgetMapping>>(emptyMap())
    val mappings: StateFlow<Map<String, BudgetMapping>> = _mappings.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private var existingMapping: PlaidCategoryMapping? = null

    val plaidCategories = PlaidCategories.TOP_LEVEL

    fun load() {
        launchCatching {
            val ownerId = authRepository.currentUser?.uid ?: return@launchCatching

            val budgetList = budgetRepository
                .getBudgets(authRepository.currentUserIdFlow)
                .first()
            _budgets.value = budgetList

            val mapping = mappingRepository.getMapping(ownerId)
            existingMapping = mapping
            _mappings.value = mapping?.mappings ?: emptyMap()
        }
    }

    fun setMapping(plaidCategory: String, budget: Budget?) {
        val current = _mappings.value.toMutableMap()
        if (budget == null) {
            current.remove(plaidCategory)
        } else {
            current[plaidCategory] = BudgetMapping(
                budgetId = budget.id ?: "",
                budgetName = budget.category
            )
        }
        _mappings.value = current
    }

    fun save() {
        launchCatching {
            val ownerId = authRepository.currentUser?.uid ?: return@launchCatching
            _isSaving.value = true
            try {
                val mapping = PlaidCategoryMapping(
                    id = existingMapping?.id,
                    ownerId = ownerId,
                    mappings = _mappings.value
                )
                mappingRepository.saveMapping(mapping)
                _saveSuccess.value = true
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun onSaveSuccessShown() {
        _saveSuccess.value = false
    }
}
