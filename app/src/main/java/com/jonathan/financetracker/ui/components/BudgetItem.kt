package com.jonathan.financetracker.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme

@SuppressLint("DefaultLocale")
@Composable
fun BudgetItem(
    budget: Budget,
    spentAmount: Double,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    val progress = if (budget.amount > 0) (spentAmount / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = spentAmount > budget.amount
    val remaining = budget.amount - spentAmount

    // Color shifts: green → yellow → red as spending increases
    val progressColor = when {
        isOverBudget -> MaterialTheme.colorScheme.error
        progress > 0.75f -> Color(0xFFE6A817) // amber warning
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = { budget.id?.let { onItemClick(it) } },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: category name + budget goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$${String.format("%.2f", budget.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row: spent + remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: $${String.format("%.2f", spentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isOverBudget)
                        "Over by $${String.format("%.2f", -remaining)}"
                    else
                        "Left: $${String.format("%.2f", remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isOverBudget) FontWeight.Medium else FontWeight.Normal,
                    color = if (isOverBudget)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BudgetItemPreview() {
    FinanceTrackerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BudgetItem(
                budget = Budget(id = "1", category = "Groceries", amount = 200.00),
                spentAmount = 150.00,
                onItemClick = {}
            )
            BudgetItem(
                budget = Budget(id = "2", category = "Dining Out", amount = 100.00),
                spentAmount = 95.00,
                onItemClick = {}
            )
            BudgetItem(
                budget = Budget(id = "3", category = "Entertainment", amount = 50.00),
                spentAmount = 65.00,
                onItemClick = {}
            )
        }
    }
}
