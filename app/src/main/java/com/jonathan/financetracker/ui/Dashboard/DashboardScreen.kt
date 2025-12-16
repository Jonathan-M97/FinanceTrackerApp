package com.jonathan.financetracker.ui.Dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.ui.components.FinanceAppBar
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.Budget.BudgetsScreen
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.screens.TransactionsScreen
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.serialization.Serializable



enum class FinancePage(val title: String) {
    TRANSACTIONS("Transactions"),
    BUDGETS("Budgets")
}

@Serializable
object DashboardRoute

@Composable
fun DashboardScreen(
    openSettingsScreen: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {

    val isLoadingUser by viewModel.isLoadingUser.collectAsStateWithLifecycle()

    if (isLoadingUser) {
        LoadingIndicator()
    } else {

        val transactions = viewModel.Transactions.collectAsStateWithLifecycle(emptyList())
        val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()

        DashboardScreenContent(
            transactions = transactions.value,
            openSettingsScreen = openSettingsScreen,
            isAnonymous = isAnonymous
//            updateItem = viewModel::updateItem
        )

    }


    LaunchedEffect(true) {
        viewModel.loadCurrentUser()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    transactions: List<Transaction>,

    openSettingsScreen: () -> Unit,

    isAnonymous: Boolean,

//    updateItem: (Transaction) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var currentPage by remember { mutableStateOf(FinancePage.TRANSACTIONS) }
    var showDialog by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = "Finance Tracker",
                icon = Icons.Filled.Settings,
                iconDescription = "Settings",
                action = openSettingsScreen,
                scrollBehavior = scrollBehavior
            )
//            FinanceAppBar(
//                currentScreen = currentPage.title,
//                onSignOutClick = onSignOut
//            )
        },
//        floatingActionButton = {
//            if (currentPage == FinancePage.TRANSACTIONS) {
//                FloatingActionButton(onClick = { showDialog = true }) {
//                    Icon(Icons.Filled.Add, "Add Transaction")
//                }
//            } else if (currentPage == FinancePage.BUDGETS) {
//            FloatingActionButton(onClick = { showDialog = true }) {
//                Icon(Icons.Filled.Add, "Add Budget")
//            }
//        }
//        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            if (isAnonymous) {
                Text(
                    text = "Using Guest Account",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { currentPage = FinancePage.TRANSACTIONS }) {
                    Text("Transactions")
                }
                Button(onClick = { currentPage = FinancePage.BUDGETS }) {
                    Text("Budgets")
                }
            }

            when (currentPage) {
                FinancePage.TRANSACTIONS -> {
                    TransactionsScreen(
                        showAddTransactionDialog = showDialog,
                        refreshKey = refreshKey,
                        onDialogDismiss = { showDialog = false },
                        onDialogConfirm = {
                            refreshKey++
                            showDialog = false
                        }
                    )
                }
                FinancePage.BUDGETS -> {
                    BudgetsScreen(
                        showAddBudgetDialog = showDialog,
                        refreshKey = refreshKey,
                        onDialogDismiss = { showDialog = false },
                        onDialogConfirm = {
                            refreshKey++
                            showDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    FinanceTrackerTheme {
        DashboardScreen(
            openSettingsScreen = {}
        )

    }
}