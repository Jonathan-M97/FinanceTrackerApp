package com.jonathan.financetracker.ui.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.StandardButton
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.format

@Serializable
data class AddTransactionRoute (val itemId: String)

@Composable
fun AddTransactionScreen(
    openDashboard: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {

    val navigateDashboard by viewModel.navigateDashboard.collectAsStateWithLifecycle()

    if (navigateDashboard) {
        openDashboard()
    } else {
        val transactionItem by viewModel.transactionItem.collectAsStateWithLifecycle()

        AddTransactionScreen(
            transactionItem = transactionItem,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = viewModel::saveItem,
//            delete = viewModel::delete, todo add delete function in viewmodel
            loadItem = viewModel::loadItem
        )
    }
}

@Composable
fun AddTransactionScreen(
    transactionItem: Transaction?,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Transaction, (ErrorMessage) -> Unit) -> Unit,
//    delete: () -> Unit, todo add delete function
    loadItem: () -> Unit
) {
    if (transactionItem == null) {
        LoadingIndicator()
    } else {
        AddTransactionScreenContent(
            transactionItem = transactionItem,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = saveItem,
//            deleteItem = deleteItem
        )
    }

    LaunchedEffect(true) {
        loadItem()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreenContent(
    transactionItem: Transaction,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Transaction, (ErrorMessage) -> Unit) -> Unit,
//    deleteItem: () -> Unit
) {

    val editableItem = remember {
        // If the transaction date is empty, it\'s a new item, so set today\'s date.
        val initialDate = if (transactionItem.date.isEmpty()) {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            LocalDate.now().format(formatter)
        } else {
            transactionItem.date
        }
        mutableStateOf(transactionItem.copy(date = initialDate))
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val focusRequester = remember { FocusRequester() }
    val amountTextFieldValue = remember {
        mutableStateOf(
            TextFieldValue(
                text = editableItem.value.amount.toString(),
                selection = TextRange(editableItem.value.amount.toString().length)
            )
        )
    }


    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = "Add Transaction",
                icon = Icons.Filled.Settings,
                iconDescription = "Settings",
                action = {
                    saveItem(editableItem.value, showErrorSnackbar)
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Add New Transaction", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = editableItem.value.description,
                onValueChange = { editableItem.value = editableItem.value.copy(description = it) },
                label = { Text("Description") }
            )
            OutlinedTextField(
                value = amountTextFieldValue.value,
                onValueChange = { newValue ->
                    if (newValue.text.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        amountTextFieldValue.value = newValue
                        editableItem.value = editableItem.value.copy(
                            amount = newValue.text.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            val text = amountTextFieldValue.value.text
                            amountTextFieldValue.value = amountTextFieldValue.value.copy(
                                selection = TextRange(0, text.length)
                            )
                        }
                    }
            )
            OutlinedTextField(
                value = editableItem.value.date,
                onValueChange = { editableItem.value = editableItem.value.copy(date = it) },
                label = { Text("Date") }
            )
            OutlinedTextField(
                value = editableItem.value.type,
                onValueChange = { editableItem.value = editableItem.value.copy(type = it) },
                label = { Text("type") }
            )

            StandardButton(
                label = R.string.add_transaction,
                onButtonClick = {
                    saveItem(editableItem.value, showErrorSnackbar)
                }
            )
        }
    }
}
