package com.jonathan.financetracker.ui.Login

import com.jonathan.financetracker.MainViewModel
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel

class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)

    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    fun signIn(
        email: String,
        password: String,
        showErrorSnackBar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackBar) {
            authRepository.signIn(email, password)
            _shouldRestartApp.value = true
        }
    }

}