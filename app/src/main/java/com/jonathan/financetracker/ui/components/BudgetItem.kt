package com.jonathan.financetracker.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme

@Composable
fun BudgetItem(
    budget: Budget,
    spentAmount: Double,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    Row (
        modifier = Modifier
            .clickable { budget.id?.let { onItemClick(it) } }
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Text(
            text = budget.category,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Budgeted: ${budget.amount}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Spent: $spentAmount",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BudgetItemPreview() {
    FinanceTrackerTheme {
        BudgetItem(
            budget = Budget(id = "asdf", category = "Groceries", amount = 200.00),
            spentAmount = 150.00,
            onItemClick = {}
        )
    }
}