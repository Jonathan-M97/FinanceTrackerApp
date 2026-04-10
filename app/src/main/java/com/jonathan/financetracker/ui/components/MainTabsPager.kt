package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jonathan.financetracker.ui.Budget.BudgetsScreen
import com.jonathan.financetracker.ui.Dashboard.DashboardScreen
import com.jonathan.financetracker.ui.Transactions.TransactionsScreen
import com.jonathan.financetracker.ui.settings.SettingsScreen

@Composable
fun MainTabsPager(
    pagerState: PagerState,
    openAddTransactionScreen: (String) -> Unit,
    openAddBudgetScreen: (String) -> Unit,
    openDashboard: () -> Unit,
    openSignInScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 1
    ) { page ->
        when (page) {
            0 -> DashboardScreen()
            1 -> TransactionsScreen(openAddTransactionScreen = openAddTransactionScreen)
            2 -> BudgetsScreen(
                openAddTransactionScreen = openAddTransactionScreen,
                openAddBudgetScreen = openAddBudgetScreen
            )
            3 -> SettingsScreen(
                openDashboard = openDashboard,
                openSignInScreen = openSignInScreen
            )
        }
    }
}
