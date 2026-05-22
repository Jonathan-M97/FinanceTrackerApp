package com.jonathan.financetracker.ui.settings

import android.app.Application
import com.google.firebase.functions.FirebaseFunctionsException
import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.model.LinkedAccount
import com.jonathan.financetracker.data.model.SyncResult
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.PlaidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
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

    private val _isUpdateMode = MutableStateFlow(false)
    val isUpdateMode: StateFlow<Boolean> = _isUpdateMode.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncResultMessage = MutableStateFlow<String?>(null)
    val syncResultMessage: StateFlow<String?> = _syncResultMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private fun showError(error: ErrorMessage) {
        _errorMessage.value = when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> application.getString(error.message)
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
        launchCatching(::showPlaidError) {
            _linkedAccounts.value = plaidRepository.getLinkedAccounts()
        }
    }

    fun createLinkToken() {
        launchCatching(::showPlaidError) {
            _isUpdateMode.value = false
            val token = plaidRepository.createLinkToken()
            _linkToken.value = token
        }
    }

    fun createUpdateLinkToken(itemId: String) {
        launchCatching(::showPlaidError) {
            _isUpdateMode.value = true
            val token = plaidRepository.createUpdateLinkToken(itemId)
            _linkToken.value = token
        }
    }

    fun onLinkTokenConsumed() {
        _linkToken.value = null
    }

    fun onPlaidLinkError(message: String?) {
        _errorMessage.value = message
            ?: application.getString(R.string.error_plaid_link_failed)
    }

    /**
     * Called from the Plaid launcher whenever the Link flow returns
     * (success or exit), so update-mode state doesn't leak across flows.
     */
    fun onPlaidLinkExit() {
        _isUpdateMode.value = false
    }

    /**
     * Called when Plaid Link returns successfully from an update-mode
     * flow. Update mode does not issue a new public_token — the existing
     * access_token is now re-authorized server-side. We immediately
     * trigger a sync so the item's lastSyncStatus flips from
     * "needs_reauth" to "ok" and the UI's red outline turns green
     * without making the user manually tap Sync.
     */
    fun onPlaidUpdateComplete() {
        _isUpdateMode.value = false
        syncTransactions()
    }

    fun exchangePublicToken(
        publicToken: String,
        institutionName: String,
        institutionId: String
    ) {
        launchCatching(::showPlaidError) {
            plaidRepository.exchangePublicToken(publicToken, institutionName, institutionId)
            loadLinkedAccounts()
        }
    }

    fun syncTransactions() {
        launchCatching(::showPlaidError) {
            _isSyncing.value = true
            try {
                val result: SyncResult = plaidRepository.syncTransactions()
                _syncResultMessage.value = if (result.failed > 0) {
                    application.getString(
                        R.string.sync_partial_message,
                        result.added,
                        result.failed
                    )
                } else {
                    application.getString(R.string.sync_success_message, result.added)
                }
                // Refresh linked accounts so per-item status colors update
                // with whatever the sync wrote to Firestore.
                _linkedAccounts.value = plaidRepository.getLinkedAccounts()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun showPlaidError(error: ErrorMessage) {
        val message = when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> application.getString(error.message)
        }
        // Check for specific Plaid error codes returned by Cloud Functions
        _errorMessage.value = when {
            message.contains("ITEM_LOGIN_REQUIRED") ->
                application.getString(R.string.error_bank_connection_expired)
            else -> message
        }
    }

    fun onSyncMessageShown() {
        _syncResultMessage.value = null
    }

    fun unlinkAccount(itemId: String) {
        launchCatching(::showPlaidError) {
            plaidRepository.unlinkAccount(itemId)
            loadLinkedAccounts()
        }
    }

    fun purgeSyncedTransactions() {
        launchCatching(::showPlaidError) {
            _isSyncing.value = true
            try {
                val count = plaidRepository.purgeSyncedTransactions()
                _syncResultMessage.value = "Deleted $count synced transactions. Sync again to re-fetch."
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
