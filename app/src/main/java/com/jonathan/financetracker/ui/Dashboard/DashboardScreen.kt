package com.jonathan.financetracker.ui.Dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.ui.components.EmptyStateMessage
import com.jonathan.financetracker.ui.components.ExpensePieChart
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.MonthNavigator
import com.jonathan.financetracker.ui.components.NetBalanceCard
import com.jonathan.financetracker.ui.components.TransactionItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.serialization.Serializable
import java.time.YearMonth

@Serializable
object DashboardRoute

@Composable
fun DashboardScreen(
    openAddTransactionScreen: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val isLoadingUser by viewModel.isLoadingUser.collectAsStateWithLifecycle()

    if (isLoadingUser) {
        LoadingIndicator()
    } else {
        val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()
        val spentAmounts by viewModel.spentAmounts.collectAsStateWithLifecycle()
        val totalMonthlySpentAmount by viewModel.totalMonthlySpentAmount.collectAsStateWithLifecycle()
        val totalMonthlyIncomeAmount by viewModel.totalMonthlyIncomeAmount.collectAsStateWithLifecycle()
        val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
        val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
        val isLoadingData by viewModel.isLoadingData.collectAsStateWithLifecycle()

        DashboardScreenContent(
            isAnonymous = isAnonymous,
            isLoadingData = isLoadingData,
            spentAmounts = spentAmounts,
            totalMonthlySpentAmount = totalMonthlySpentAmount,
            totalMonthlyIncomeAmount = totalMonthlyIncomeAmount,
            recentTransactions = recentTransactions,
            selectedMonth = selectedMonth,
            onPreviousMonthClick = { viewModel.goToPreviousMonth() },
            onNextMonthClick = { viewModel.goToNextMonth() },
            canGoToNextMonth = viewModel.canGoToNextMonth(),
            onTransactionClick = openAddTransactionScreen,
            modifier = modifier
        )
    }

    LaunchedEffect(true) {
        viewModel.loadCurrentUser()
    }
}

@Composable
fun DashboardScreenContent(
    isAnonymous: Boolean,
    isLoadingData: Boolean,
    spentAmounts: Map<String, Double>,
    totalMonthlySpentAmount: Double,
    totalMonthlyIncomeAmount: Double,
    recentTransactions: List<Transaction>,
    selectedMonth: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    canGoToNextMonth: Boolean,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (isAnonymous) {
                Text(
                    text = "Using Guest Account",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    textAlign = TextAlign.Center
                )
            }

            MonthNavigator(
                selectedMonth = selectedMonth,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick,
                canGoToNext = canGoToNextMonth,
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
            )

            NetBalanceCard(
                totalIncome = totalMonthlyIncomeAmount,
                totalExpenses = totalMonthlySpentAmount,
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))

            if (isLoadingData) {
                LoadingIndicator()
            } else if (spentAmounts.isEmpty()) {
                EmptyStateMessage(
                    message = stringResource(R.string.empty_dashboard),
                    icon = Icons.Default.PieChart
                )
            } else {
                ExpensePieChart(
                    data = spentAmounts,
                    total = totalMonthlySpentAmount,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                )
            }

            if (recentTransactions.isNotEmpty()) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.padding_medium),
                        vertical = dimensionResource(R.dimen.padding_small)
                    )
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
                ) {
                    recentTransactions.forEach { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onItemClick = onTransactionClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    FinanceTrackerTheme {
        DashboardScreen()
    }
}
