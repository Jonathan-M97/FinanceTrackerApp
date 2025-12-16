package com.jonathan.financetracker.ui.addtransaction

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel

class AddTransactionViewModel @Inject constructor(

    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository

) : MainViewModel() {
    private val _navigateDashboard = MutableStateFlow(false)
    val navigateDashboard: StateFlow<Boolean>
        get() = _navigateDashboard.asStateFlow()

    private val addTransactionRouteArgs = savedStateHandle.toRoute<AddTransactionRoute>()
    private val itemId: String = addTransactionRouteArgs.itemId


    private val _transactionItem = MutableStateFlow<Transaction?>(null)
    val transactionItem: StateFlow<Transaction?>
        get() = _transactionItem.asStateFlow()



    fun loadItem() {
        launchCatching {
            if (itemId.isBlank()) {
                _transactionItem.value = Transaction()
            } else {
                _transactionItem.value = transactionRepository.getTransaction(itemId)
            }
        }
    }

    fun saveItem(
        item: Transaction,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        val ownerId = authRepository.currentUser?.uid

        if (ownerId.isNullOrBlank()) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_missing_owner_id))
            return
        }

        if (item.description.isBlank()) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.error_missing_description))
            return
        }

        launchCatching {
            val newItem = item.copy(ownerId = ownerId)
            if (itemId.isBlank()) {
                transactionRepository.create(newItem)
            } else {
//                transactionRepository.update(newItem) todo add update function

            }

            _navigateDashboard.value = true
        }
    }




}