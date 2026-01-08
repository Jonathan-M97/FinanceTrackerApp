package com.jonathan.financetracker.ui.addtransaction

import android.icu.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.DeleteButton
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.StandardButton
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val budgetList by viewModel.budgetList.collectAsStateWithLifecycle()


        AddTransactionScreen(
            transactionItem = transactionItem,
            budgetList = budgetList,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = viewModel::saveItem,
            deleteItem = viewModel::deleteItem,
            loadItem = viewModel::loadItem
        )
    }
}

@Composable
fun AddTransactionScreen(
    transactionItem: Transaction?,
    budgetList: List<String>,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Transaction, (ErrorMessage) -> Unit) -> Unit,
    deleteItem: (Transaction, (ErrorMessage) -> Unit) -> Unit,
    loadItem: () -> Unit
) {
    if (transactionItem == null) {
        LoadingIndicator()
    } else {
        AddTransactionScreenContent(
            transactionItem = transactionItem,
            budgetList = budgetList,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = saveItem,
            deleteItem = deleteItem
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
    budgetList: List<String>,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Transaction, (ErrorMessage) -> Unit) -> Unit,
    deleteItem: (Transaction, (ErrorMessage) -> Unit) -> Unit
) {

    val editableItem = remember(transactionItem) { mutableStateOf(transactionItem) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val focusRequester = remember { FocusRequester() }
    val amountTextFieldValue = remember(transactionItem) {
        mutableStateOf(
            TextFieldValue(
                text = editableItem.value.amount.toString(),
                selection = TextRange(editableItem.value.amount.toString().length)
            )
        )
    }

    // State for the dropdown menu
    val isTypeDropdownExpanded = remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var dateString by remember(editableItem.value.date) {
        mutableStateOf(dateFormat.format(editableItem.value.date))
    }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.time = editableItem.value.date

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            editableItem.value = editableItem.value.copy(date = calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                value = dateString,
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier.clickable { datePickerDialog.show() }
            )

            ExposedDropdownMenuBox(
                expanded = isTypeDropdownExpanded.value,
                onExpandedChange = { isTypeDropdownExpanded.value = !isTypeDropdownExpanded.value }
            ) {
                OutlinedTextField(
                    value = editableItem.value.type,
                    onValueChange = {}, // Keep empty to prevent manual typing
                    readOnly = true, // Make it non-editable
                    label = { Text("Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded.value)
                    },
                    modifier = Modifier.menuAnchor() // Important for accessibility
                )
                ExposedDropdownMenu(
                    expanded = isTypeDropdownExpanded.value,
                    onDismissRequest = { isTypeDropdownExpanded.value = false }
                ) {
                    budgetList.forEach { budget ->
                        DropdownMenuItem(
                            text = { Text(budget) },
                            onClick = {
                                editableItem.value = editableItem.value.copy(type = budget)
                                isTypeDropdownExpanded.value = false
                            }
                        )
                    }
                }
            }

            StandardButton(
                label = if (transactionItem.id == null) R.string.add_transaction else R.string.update_transaction,
                onButtonClick = {
                    val item = editableItem.value
                    val isDescriptionBlank = item.description.isBlank()
                    val isAmountZero = item.amount == 0.0
                    val isTypeBlank = item.type.isBlank()

                    // validate that all fields are filled
                    if (isDescriptionBlank || isAmountZero || isTypeBlank) {
                        showErrorSnackbar(ErrorMessage.IdError(R.string.error_missing_fields))
                    } else {
                        saveItem(editableItem.value, showErrorSnackbar)
                    }
                }
            )

            // This button will only be composed if the budgetItem.id is not null
            if (transactionItem.id != null) {
                DeleteButton (
                    label = R.string.delete_transaction,
                    onButtonClick = {
                        deleteItem(editableItem.value, showErrorSnackbar)
                    }
                )
            }
        }
    }
}
