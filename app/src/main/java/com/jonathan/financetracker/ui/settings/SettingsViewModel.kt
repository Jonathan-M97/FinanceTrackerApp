package com.jonathan.financetracker.ui.settings

import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.model.LinkedAccount
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.PlaidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val plaidRepository: PlaidRepository
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean>
        get() = _isAnonymous.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _linkedAccounts = MutableStateFlow<List<LinkedAccount>>(emptyList())
    val linkedAccounts: StateFlow<List<LinkedAccount>> = _linkedAccounts.asStateFlow()

    private val _linkToken = MutableStateFlow<String?>(null)
    val linkToken: StateFlow<String?> = _linkToken.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncResultMessage = MutableStateFlow<String?>(null)
    val syncResultMessage: StateFlow<String?> = _syncResultMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private fun showError(error: ErrorMessage) {
        _errorMessage.value = when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> "Something went wrong"
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun loadCurrentUser() {
        launchCatching(::showError) {
            val currentUser = authRepository.currentUser
            _isAnonymous.value = currentUser != null && currentUser.isAnonymous
            _userEmail.value = currentUser?.email
        }
    }

    fun signOut() {
        launchCatching(::showError) {
            authRepository.signOut()
            _shouldRestartApp.value = true
        }
    }

    fun deleteAccount() {
        launchCatching(::showError) {
            authRepository.deleteAccount()
            _shouldRestartApp.value = true
        }
    }

    // ─── Plaid ───────────────────────────────────────────────────────

    fun loadLinkedAccounts() {
        launchCatching(::showError) {
            _linkedAccounts.value = plaidRepository.getLinkedAccounts()
        }
    }

    fun createLinkToken() {
        launchCatching(::showError) {
            val token = plaidRepository.createLinkToken()
            _linkToken.value = token
        }
    }

    fun onLinkTokenConsumed() {
        _linkToken.value = null
    }

    fun exchangePublicToken(
        publicToken: String,
        institutionName: String,
        institutionId: String
    ) {
        launchCatching(::showError) {
            plaidRepository.exchangePublicToken(publicToken, institutionName, institutionId)
            loadLinkedAccounts()
        }
    }

    fun syncTransactions() {
        launchCatching(::showError) {
            _isSyncing.value = true
            val count = plaidRepository.syncTransactions()
            _syncResultMessage.value = "Synced $count transactions."
            _isSyncing.value = false
        }
    }

    fun onSyncMessageShown() {
        _syncResultMessage.value = null
    }

    fun unlinkAccount(itemId: String) {
        launchCatching(::showError) {
            plaidRepository.unlinkAccount(itemId)
            loadLinkedAccounts()
        }
    }
}
