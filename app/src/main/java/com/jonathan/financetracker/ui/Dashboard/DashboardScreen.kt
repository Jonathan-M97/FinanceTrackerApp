package com.jonathan.financetracker.ui.Dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.LoadingIndicator
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.serialization.Serializable

@Serializable
object DashboardRoute

@Composable
fun DashboardScreen(
    openSettingsScreen: () -> Unit,
    openBudgetScreen: () -> Unit,
    openAddTransactionScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
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
            openBudgetScreen = openBudgetScreen,
            openAddTransactionScreen = openAddTransactionScreen,
            isAnonymous = isAnonymous,
//            updateItem = viewModel::updateItem,
            modifier = modifier
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

    openBudgetScreen: () -> Unit,

    openAddTransactionScreen: (String) -> Unit,

    isAnonymous: Boolean,

//    updateItem: (Transaction) -> Unit

    modifier: Modifier = Modifier
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = "Finance Tracker",
                icon = Icons.Filled.Settings,
                iconDescription = "Settings",
                action = openSettingsScreen,
                scrollBehavior = scrollBehavior
            )
        },
        // Add a transaction button
        floatingActionButton = {
            FloatingActionButton(onClick = { openAddTransactionScreen("") }) {
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

            // button to switch to budgets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(onClick = { openBudgetScreen() }) {
                    Text("Budgets")
                }
            }


            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                modifier = modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Text (
                    text = transaction.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text (
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text (
                    text = transaction.amount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    FinanceTrackerTheme {
        DashboardScreen(
            openSettingsScreen = {},
            openBudgetScreen = {},
            openAddTransactionScreen = {""}
        )

    }
}