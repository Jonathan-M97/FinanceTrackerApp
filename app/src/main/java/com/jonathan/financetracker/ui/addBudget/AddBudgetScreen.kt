package com.jonathan.financetracker.ui.addBudget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.StandardButton
import kotlinx.serialization.Serializable

@Serializable
data class AddBudgetRoute (val itemId: String)

@Composable
fun AddBudgetScreen(
    openDashboard: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddBudgetViewModel = hiltViewModel()
) {

    val navigateDashboard by viewModel.navigateDashboard.collectAsStateWithLifecycle()

    if (navigateDashboard) {
        openDashboard()
    } else {
        val budgetItem by viewModel.budgetItem.collectAsStateWithLifecycle()

        AddBudgetScreen(
            budgetItem = budgetItem,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = viewModel::saveItem,
//            delete = viewModel::delete, todo add delete function in view model
            loadItem = viewModel::loadItem
        )
    }
}

@Composable
fun AddBudgetScreen(
    budgetItem: Budget?,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Budget, (ErrorMessage) -> Unit) -> Unit,
//    delete: () -> Unit, todo add delete function
    loadItem: () -> Unit
) {
    if (budgetItem == null) {
        LoadingIndicator()
    } else {
        AddBudgetScreenContent(
            budgetItem = budgetItem,
            showErrorSnackbar = showErrorSnackbar,
            saveItem = saveItem
//            deleteItem = deleteItem
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreenContent(
    budgetItem: Budget,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    saveItem: (Budget, (ErrorMessage) -> Unit) -> Unit,
//    deleteItem: () -> Unit
) {
    val editableItem = remember { mutableStateOf(budgetItem) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = "Add Budget",
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
            Text("Add New Budget", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = editableItem.value.category,
                onValueChange = { editableItem.value = editableItem.value.copy(category = it) },
                label = { Text("Category") }
            )
            OutlinedTextField(
                value = editableItem.value.amount.toString(),
                onValueChange = {
                    editableItem.value =
                        editableItem.value.copy(amount = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Amount") }
            )

            StandardButton(
                label = R.string.add_budget,
                onButtonClick = {
                    saveItem(editableItem.value, showErrorSnackbar)
                }
            )
        }
    }
}