package com.jonathan.financetracker.ui.Budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.addBudgetToFirestore
import com.jonathan.financetracker.ui.Dashboard.DashboardScreen
import com.jonathan.financetracker.ui.Dashboard.TransactionItem
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Serializable
object BudgetRoute

@Composable
fun BudgetsScreen(
    openDashboard: () -> Unit,
    openSettingsScreen: () -> Unit,
    openAddBudgetScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val isLoadingUser by viewModel.isLoadingUser.collectAsStateWithLifecycle()

    if(isLoadingUser) {
        LoadingIndicator()
    } else {
        val budgets = viewModel.Budgets.collectAsStateWithLifecycle(emptyList())
        val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()

        BudgetsScreenContent(
            budgets = budgets.value,
            openDashboard = openDashboard,
            openSettingsScreen = openSettingsScreen,
            openAddBudgetScreen = openAddBudgetScreen,
            isAnonymous = isAnonymous,
            modifier = modifier
        )

    }

    LaunchedEffect(true) {
        viewModel.loadCurrentUser()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreenContent(
    budgets: List<Budget>,
    openDashboard: () -> Unit,
    openSettingsScreen: () -> Unit,
    openAddBudgetScreen: (String) -> Unit,
    isAnonymous: Boolean,
    modifier: Modifier = Modifier
){
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = "Budgets/Goals",
                icon = Icons.Filled.Settings,
                iconDescription = "Settings",
                action = openSettingsScreen,
                scrollBehavior = scrollBehavior
            )
        },
        // Add a transaction button todo
        floatingActionButton = {
            FloatingActionButton(onClick = { openAddBudgetScreen("") }) {
                Icon(Icons.Filled.Add, "Add Budget")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
            // checks if using guest account or not and displays message accordingly
            if (isAnonymous) {
                Text(
                    text = "Using Guest Account",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    textAlign = TextAlign.Center
                )
            }

            // button to switch to budgets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(onClick = { openDashboard() }) {
                    Text("Transaction")
                }
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                modifier = modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
            ) {
                items(budgets) { budget ->
                    BudgetItem(
                        budget = budget
                    )
                }
            }


        }

    }
}

@Composable
fun BudgetItem(
    budget: Budget,
    modifier: Modifier = Modifier
) {
    Card (
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)

        ) {
            Text(
                text = budget.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = budget.amount.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    FinanceTrackerTheme {
        BudgetsScreen(
            openDashboard = {},
            openSettingsScreen = {""},
            openAddBudgetScreen = {""}
        )

    }
}


//@Composable
//fun BudgetsScreen(
//    showAddBudgetDialog: Boolean,
//    refreshKey: Int,
//    onDialogDismiss: () -> Unit,
//    onDialogConfirm: () -> Unit,
//    modifier: Modifier = Modifier
//) {
////    var showDialog by remember { mutableStateOf(false) }
////    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }
//
//    val budgetState = produceState<List<Budget>>(initialValue = emptyList(), key1 = refreshKey) {
//        val firebaseApp = Firebase.app
//        val db = Firebase.firestore(firebaseApp, "financetracker")
//        value = db.collection("budgets").get().await().toObjects(Budget::class.java)
//    }
//
//
//
//
//    if (showAddBudgetDialog) {
//        AddBudgetDialog(
//            onDismiss = onDialogDismiss,
//            onConfirm = { newBudget ->
//                addBudgetToFirestore(newBudget)
//                onDialogConfirm()
//            }
//        )
//    }
//
//    LazyColumn(
//        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
//        modifier = modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
//    ) {
//        items(budgetState.value) { budget ->
//            BudgetItem(
//                budget = budget
//            )
//        }
//    }
//}