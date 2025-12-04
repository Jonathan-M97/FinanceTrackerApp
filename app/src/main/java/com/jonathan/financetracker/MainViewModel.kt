package com.jonathan.financetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.jonathan.financetracker.data.model.ErrorMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.jonathan.financetracker.R

open class MainViewModel : ViewModel() {
    fun launchCatching(
        showErrorSnackbar: (ErrorMessage) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit
    ) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Firebase.crashlytics.recordException(throwable)
                val error = if (throwable.message.isNullOrBlank()) {
                    ErrorMessage.IdError(R.string.generic_error)
                } else {
                    ErrorMessage.StringError(throwable.message!!)
                }
                showErrorSnackbar(error)
            },
            block = block
        )
}