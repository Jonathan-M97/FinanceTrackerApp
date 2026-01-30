package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExpensePieChart(
    data: Map<String, Double>,
    total: Double,
    modifier: Modifier = Modifier
) {
    // Use AndroidView to embed the MPAndroidChart PieChart
    AndroidView(
        factory = { context ->
            // Factory block: creates the view
            PieChart(context).apply {
                // Basic chart setup
                description.isEnabled = false
                isDrawHoleEnabled = true // Creates a donut chart
                legend.isEnabled = false // We can hide the default legend if we want
                setEntryLabelColor(Color.Black.toArgb())

                // Enable drawing text in the center of the chart
                setDrawCenterText(true)
                centerText = "Total" // Initial text, will be updated
                setCenterTextSize(16f)
            }
        },
        update = { chart ->
            // Update block: called when the data changes
            val entries = data.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }


            // Format the total amount as currency
            val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(total)
            chart.centerText = "Total$formattedTotal"

            val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                // Use a predefined color palette
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextColor = Color.Black.toArgb()
                valueTextSize = 12f
                setDrawValues(false)
            }

            chart.data = PieData(dataSet)
            // Refresh the chart
            chart.invalidate()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp) // Give the chart a fixed height
    )
}
