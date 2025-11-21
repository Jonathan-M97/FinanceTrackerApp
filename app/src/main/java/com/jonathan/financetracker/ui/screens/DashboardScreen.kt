package com.jonathan.financetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.jonathan.financetracker.R
import com.jonathan.financetracker.ui.components.FinanceAppBar
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme


enum class FinancePage(val title: String) {
    TRANSACTIONS("Transactions"),
    BUDGETS("Budgets")
}

@Composable
fun DashboardScreen(
    onSignOut: () -> Unit
) {
    var currentPage by remember { mutableStateOf(FinancePage.TRANSACTIONS) }
    var showDialog by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            FinanceAppBar(
                currentScreen = currentPage.title,
                onSignOutClick = onSignOut
            )
        },
        floatingActionButton = {
            if (currentPage == FinancePage.TRANSACTIONS) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Add, "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
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
                    BudgetsScreen()
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
            onSignOut = { }
        )
    }
}