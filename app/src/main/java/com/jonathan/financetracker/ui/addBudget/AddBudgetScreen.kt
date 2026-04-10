package com.jonathan.financetracker.ui.addBudget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.DeleteButton
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.StandardButton
import kotlinx.serialization.Serializable

@Serializable
data class AddBudgetRoute (val itemId: String)

@Composable
fun AddBudgetScreen(
    openDashboard: () -> Unit,
    openBudget: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddBudgetViewModel = hiltViewModel()
) {

    val navigateDashboard by viewModel.navigateDashboard.collectAsStateWithLifecycle()
    val navigateBudget by viewModel.navigateBudget.collectAsStateWithLifecycle()

    if (navigateDashboard) {
        openDashboard()
    } else if (navigateBudget){
        openBudget()
    } else {
        val budgetItem by viewModel.budgetItem.collectAsStateWithLifecycle()
        val linkedCategories by viewModel.linkedCategories.collectAsStateWithLifecycle()

        AddBudgetScreen(
            budgetItem = budgetItem,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = viewModel::saveItem,
            deleteItem = viewModel::deleteItem,
            loadItem = viewModel::loadItem,
            plaidCategories = viewModel.plaidCategories,
            linkedCategories = linkedCategories,
            onTogglePlaidCategory = viewModel::togglePlaidCategory
        )
    }
}

@Composable
fun AddBudgetScreen(
    budgetItem: Budget?,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Budget, (ErrorMessage) -> Unit) -> Unit,
    deleteItem: (Budget, (ErrorMessage) -> Unit) -> Unit,
    loadItem: () -> Unit,
    plaidCategories: List<String>,
    linkedCategories: Set<String>,
    onTogglePlaidCategory: (String) -> Unit
) {
    if (budgetItem == null) {
        LoadingIndicator()
    } else {
        AddBudgetScreenContent(
            budgetItem = budgetItem,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = saveItem,
            deleteItem = deleteItem,
            plaidCategories = plaidCategories,
            linkedCategories = linkedCategories,
            onTogglePlaidCategory = onTogglePlaidCategory
        )
    }

    LaunchedEffect(true) {
        loadItem()
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddBudgetScreenContent(
    budgetItem: Budget,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Budget, (ErrorMessage) -> Unit) -> Unit,
    deleteItem: (Budget, (ErrorMessage) -> Unit) -> Unit,
    plaidCategories: List<String>,
    linkedCategories: Set<String>,
    onTogglePlaidCategory: (String) -> Unit
) {
    val editableItem = remember { mutableStateOf(budgetItem) }
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
                title = if (budgetItem.id == null) "Add Budget" else "Edit Budget",
                icon = Icons.Filled.Settings,
                iconDescription = "Settings",
                action = {
                    saveItem(editableItem.value, showErrorSnackbar)
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (budgetItem.id == null) "Add New Budget" else "Edit Budget", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = editableItem.value.category,
                onValueChange = { editableItem.value = editableItem.value.copy(category = it) },
                label = { Text("Category") }
            )
            OutlinedTextField(
                value = amountTextFieldValue.value,
                onValueChange = { newValue ->
                    if (newValue.text.matches(Regex("^\\d*\\.?\\d*$"))) {
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

            // Plaid category linking section (only when editing)
            if (budgetItem.id != null) {
                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
                Spacer(Modifier.size(8.dp))

                Text(
                    stringResource(R.string.linked_plaid_categories),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    stringResource(R.string.category_mapping_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    plaidCategories.forEach { category ->
                        FilterChip(
                            selected = linkedCategories.contains(category),
                            onClick = { onTogglePlaidCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }

                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
            }

            Spacer(Modifier.size(8.dp))

            StandardButton(
                label = if (budgetItem.id == null) R.string.add_budget else R.string.update_budget,
                onButtonClick = {
                    saveItem(editableItem.value, showErrorSnackbar)
                }
            )

            // This button will only be composed if the budgetItem.id is not null
            if (budgetItem.id != null) {
                DeleteButton (
                    label = R.string.delete_budget,
                    onButtonClick = {
                        deleteItem(editableItem.value, showErrorSnackbar)
                    }
                )
            }
        }
    }
}
