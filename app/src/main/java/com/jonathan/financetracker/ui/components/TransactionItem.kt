package com.jonathan.financetracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme

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