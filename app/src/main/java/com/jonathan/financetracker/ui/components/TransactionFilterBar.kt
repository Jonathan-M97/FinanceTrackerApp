package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jonathan.financetracker.ui.Transactions.SortDirection
import com.jonathan.financetracker.ui.Transactions.SortField
import com.jonathan.financetracker.ui.Transactions.TransactionFilterState
import com.jonathan.financetracker.ui.Transactions.TransactionTypeFilter

private val HorizontalScrollConsumer = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        return Offset(available.x, 0f)
    }
}

private fun Modifier.fadingEdge(
    scrollState: ScrollState,
    length: Dp = 24.dp
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val leftFade = if (scrollState.value > 0) length.toPx() else 0f
        val rightFade = if (scrollState.value < scrollState.maxValue) length.toPx() else 0f
        if (leftFade > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = leftFade
                ),
                blendMode = BlendMode.DstIn
            )
        }
        if (rightFade > 0f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - rightFade,
                    endX = size.width
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

@Composable
fun TransactionFilterBar(
    filterState: TransactionFilterState,
    availableCategories: List<String>,
    onTypeFilterChange: (TransactionTypeFilter) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClearCategories: () -> Unit,
    onToggleSort: (SortField) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(HorizontalScrollConsumer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Row 1: Type filters + Sort toggles
            val typeScrollState = rememberScrollState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fadingEdge(typeScrollState)
                    .horizontalScroll(typeScrollState)
            ) {
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.ALL,
                    onClick = { onTypeFilterChange(TransactionTypeFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.INCOME,
                    onClick = { onTypeFilterChange(TransactionTypeFilter.INCOME) },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = filterState.typeFilter == TransactionTypeFilter.EXPENSE,
                    onClick = { onTypeFilterChange(TransactionTypeFilter.EXPENSE) },
                    label = { Text("Expense") }
                )

                VerticalDivider(modifier = Modifier.height(24.dp))

                FilterChip(
                    selected = filterState.sortField == SortField.DATE,
                    onClick = { onToggleSort(SortField.DATE) },
                    label = { Text("Date") },
                    trailingIcon = {
                        if (filterState.sortField == SortField.DATE) {
                            Icon(
                                imageVector = if (filterState.sortDirection == SortDirection.ASCENDING)
                                    Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (filterState.sortDirection == SortDirection.ASCENDING)
                                    "Ascending" else "Descending",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                )
                FilterChip(
                    selected = filterState.sortField == SortField.AMOUNT,
                    onClick = { onToggleSort(SortField.AMOUNT) },
                    label = { Text("Amount") },
                    trailingIcon = {
                        if (filterState.sortField == SortField.AMOUNT) {
                            Icon(
                                imageVector = if (filterState.sortDirection == SortDirection.ASCENDING)
                                    Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (filterState.sortDirection == SortDirection.ASCENDING)
                                    "Ascending" else "Descending",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                )
            }

            // Row 2: Category chips (only when categories exist)
            if (availableCategories.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                val categoryScrollState = rememberScrollState()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fadingEdge(categoryScrollState)
                        .horizontalScroll(categoryScrollState)
                ) {
                    availableCategories.forEach { category ->
                        FilterChip(
                            selected = category in filterState.selectedCategories,
                            onClick = { onToggleCategory(category) },
                            label = { Text(category.ifBlank { "Uncategorized" }) }
                        )
                    }
                    if (filterState.selectedCategories.isNotEmpty()) {
                        AssistChip(
                            onClick = onClearCategories,
                            label = { Text("Clear") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear category filter"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
