package com.jonathan.financetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jonathan.financetracker.data.model.ErrorMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

open class MainViewModel : ViewModel() {
    fun launchCatching(
        showErrorSnackbar: (ErrorMessage) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit
    ) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Firebase.crashlytics.recordException(throwable)
                val error = mapExceptionToError(throwable)
                showErrorSnackbar(error)
            },
            block = block
        )

    private fun mapExceptionToError(throwable: Throwable): ErrorMessage {
        return when (throwable) {
            is IOException ->
                ErrorMessage.IdError(R.string.error_network)
            is FirebaseAuthInvalidCredentialsException ->
                ErrorMessage.IdError(R.string.error_invalid_credentials)
            is FirebaseAuthInvalidUserException ->
                ErrorMessage.IdError(R.string.error_account_not_found)
            is FirebaseAuthUserCollisionException ->
                ErrorMessage.IdError(R.string.error_account_already_exists)
            is FirebaseFirestoreException ->
                ErrorMessage.IdError(R.string.error_database)
            is IllegalStateException ->
                ErrorMessage.IdError(R.string.error_sign_in_required)
            else ->
                ErrorMessage.IdError(R.string.generic_error)
        }
    }
}