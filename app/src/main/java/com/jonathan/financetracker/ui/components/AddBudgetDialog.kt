package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jonathan.financetracker.data.model.Budget

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    // State for each input field
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Add New Budget", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.padding(8.dp))
                    Button(onClick = {
                        val newBudget = Budget(
                            category = category,
                            amount = amount.toDoubleOrNull() ?: 0.0 // Safely convert to Double
                        )
                        onConfirm(newBudget)
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}