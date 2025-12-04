package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)

    ) {
        Text(
            text = budget.category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = budget.amount.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BudgetItemPreview() {
    FinanceTrackerTheme {
        BudgetItem(
            Budget("Groceries", 200.00))
    }
}