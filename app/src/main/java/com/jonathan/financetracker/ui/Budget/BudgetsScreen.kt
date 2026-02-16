package com.jonathan.financetracker.ui.Budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
//import androidx.core.i18n.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.components.BudgetItem
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.ExpensePieChart
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.StandardButton
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.serialization.Serializable
import java.time.YearMonth
import java.time.format.DateTimeFormatter



@Serializable
object BudgetRoute

@Composable
fun BudgetsScreen(
    openDashboard: () -> Unit,
    openSettingsScreen: () -> Unit,
    openAddTransactionScreen: (String) -> Unit,
    openAddBudgetScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val isLoadingUser by viewModel.isLoadingUser.collectAsStateWithLifecycle()

    if (isLoadingUser) {
        LoadingIndicator()
    } else {
        val budgets = viewModel.budgets.collectAsStateWithLifecycle(emptyList())
        val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()
        val spentAmounts by viewModel.spentAmounts.collectAsStateWithLifecycle()
        val totalMonthlySpentAmount by viewModel.totalMonthlySpentAmount.collectAsStateWithLifecycle()
        val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()


        BudgetsScreenContent(
            budgets = budgets.value,
            spentAmounts = spentAmounts,
            totalMonthlySpentAmount = totalMonthlySpentAmount,
            selectedMonth = selectedMonth,
            onPreviousMonthClick = {viewModel.goToPreviousMonth()},
            onNextMonthClick = {viewModel.goToNextMonth()},
            canGoToNextMonth = viewModel.canGoToNextMonth(),
            openDashboard = openDashboard,
            openSettingsScreen = openSettingsScreen,
            openAddTransactionScreen = openAddTransactionScreen,
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
    spentAmounts: Map<String, Double>,
    totalMonthlySpentAmount: Double,
    selectedMonth: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    canGoToNextMonth: Boolean,
    openDashboard: () -> Unit,
    openSettingsScreen: () -> Unit,
    openAddTransactionScreen: (String) -> Unit,
    openAddBudgetScreen: (String) -> Unit,
    isAnonymous: Boolean,
    modifier: Modifier = Modifier
) {
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

        floatingActionButton = {
            FloatingActionButton(onClick = { openAddTransactionScreen("") },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
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

            // button to switch to Transaction
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(onClick = { openDashboard() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) ) {
                    Text("Transaction")
                }
            }

            MonthNavigator(
                selectedMonth = selectedMonth,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick,
                canGoToNext = canGoToNextMonth,
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                modifier = modifier
                    .padding(
                        horizontal = dimensionResource(R.dimen.padding_small),
                        vertical = dimensionResource(R.dimen.padding_small)
                    )
                    .weight(1f)
            ) {

                item {
                    if (spentAmounts.isNotEmpty()) {
                        ExpensePieChart(
                            data = spentAmounts,
                            total = totalMonthlySpentAmount,
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                        )
                    }
                }

                items(budgets) { budget ->
                    val spentAmount = spentAmounts[budget.category] ?: 0.0
                    BudgetItem(
                        budget = budget,
                        spentAmount = spentAmount,
                        onItemClick = openAddBudgetScreen
                    )
                }
            }

            StandardButton(
                label = R.string.add_budget,
                onButtonClick = { openAddBudgetScreen("") },
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}

@Composable
fun MonthNavigator(
    selectedMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    canGoToNext: Boolean,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
        }
        Text(
            text = selectedMonth.format(formatter),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextClick, enabled = canGoToNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    FinanceTrackerTheme {
        BudgetsScreen(
            openDashboard = {},
            openSettingsScreen = {},
            openAddTransactionScreen = {},
            openAddBudgetScreen = {}
        )
    }
}
