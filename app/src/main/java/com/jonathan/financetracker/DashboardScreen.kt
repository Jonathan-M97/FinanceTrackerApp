package com.jonathan.financetracker

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathan.financetracker.data.Transaction
import com.jonathan.financetracker.data.transactions
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppBar(
    currentScreen: String,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(currentScreen,
                color = MaterialTheme.colorScheme.onPrimary
            )
                },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.statusBarsPadding(),
    )
}

@Composable
fun FinanceTrackerApp() {

    Scaffold(
        topBar = {
            FinanceAppBar(
                currentScreen = "Finance Tracker"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = dimensionResource(R.dimen.padding_small))
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailCard("Groceries", "1.00", modifier = Modifier
                        .weight(1f)
                        .padding(end = dimensionResource(R.dimen.padding_extra_small))
                    )
                    DetailCard("Mortgage", "1.00", modifier = Modifier
                            .weight(1f)
                        .padding(end = dimensionResource(R.dimen.padding_extra_small)))
                    DetailCard("Income", "1.00", modifier = Modifier
                        .weight(1f)
                        .padding(end = dimensionResource(R.dimen.padding_extra_small)))
                }

            }
            items(transactions) { transaction ->
                TransactionItem(
                    transaction = transaction
                )
            }
        }
    }
}

@Composable
fun DetailCard(
    category: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)

    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = amount,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
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

@Preview (showBackground = true)
@Composable
fun TransactionItemPreview() {
    FinanceTrackerTheme {
        TransactionItem(
            Transaction("Groceries", 12.43,"Feb 24","Groceries")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailCardPreview() {
    FinanceTrackerTheme {
        DetailCard("Groceries", "1.00", modifier = Modifier.padding(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun FinanceTrackerAppPreview() {
    FinanceTrackerTheme {
        FinanceTrackerApp()
    }
}
