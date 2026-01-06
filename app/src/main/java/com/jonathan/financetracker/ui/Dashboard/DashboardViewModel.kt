package com.jonathan.financetracker.ui.Dashboard

import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject



@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionsRepository: TransactionRepository
) : MainViewModel() {

    private val _isLoadingUser = MutableStateFlow(true)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean>
        get() = _isAnonymous.asStateFlow()

    val Transactions = transactionsRepository.getTransactions(authRepository.currentUserIdFlow)

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