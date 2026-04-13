package com.jonathan.financetracker.ui.Transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.ui.components.EmptyStateMessage
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.components.MonthNavigator
import com.jonathan.financetracker.ui.components.TransactionFilterBar
import com.jonathan.financetracker.ui.components.TransactionItem
import kotlinx.serialization.Serializable
import java.time.YearMonth

@Serializable
object TransactionsRoute

@Composable
fun TransactionsScreen(
    openAddTransactionScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()

    if (isLoading) {
        LoadingIndicator()
    } else {
        TransactionsScreenContent(
            transactions = transactions,
            selectedMonth = selectedMonth,
            filterState = filterState,
            availableCategories = availableCategories,
            onPreviousMonthClick = { viewModel.goToPreviousMonth() },
            onNextMonthClick = { viewModel.goToNextMonth() },
            canGoToNextMonth = viewModel.canGoToNextMonth(),
            onTypeFilterChange = viewModel::setTypeFilter,
            onToggleCategory = viewModel::toggleCategory,
            onClearCategories = viewModel::clearCategoryFilter,
            onToggleSort = viewModel::toggleSort,
            openAddTransactionScreen = openAddTransactionScreen,
            modifier = modifier
        )
    }
}

@Composable
fun TransactionsScreenContent(
    transactions: List<Transaction>,
    selectedMonth: YearMonth,
    filterState: TransactionFilterState,
    availableCategories: List<String>,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    canGoToNextMonth: Boolean,
    onTypeFilterChange: (TransactionTypeFilter) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClearCategories: () -> Unit,
    onToggleSort: (SortField) -> Unit,
    openAddTransactionScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column {
                MonthNavigator(
                    selectedMonth = selectedMonth,
                    onPreviousClick = onPreviousMonthClick,
                    onNextClick = onNextMonthClick,
                    canGoToNext = canGoToNextMonth,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small))
                )

                if (transactions.isEmpty()) {
                    EmptyStateMessage(
                        message = stringResource(R.string.empty_transactions),
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        modifier = modifier
                            .weight(1f)
                            .padding(horizontal = dimensionResource(R.dimen.padding_small))
                    ) {
                        items(transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onItemClick = openAddTransactionScreen
                            )
                        }
                    }
                }

                TransactionFilterBar(
                    filterState = filterState,
                    availableCategories = availableCategories,
                    onTypeFilterChange = onTypeFilterChange,
                    onToggleCategory = onToggleCategory,
                    onClearCategories = onClearCategories,
                    onToggleSort = onToggleSort
                )
            }

            FloatingActionButton(
                onClick = { openAddTransactionScreen("") },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 140.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Transaction")
            }
        }
    }
}
