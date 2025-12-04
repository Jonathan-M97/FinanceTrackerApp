package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.model.Category
import com.jonathan.financetracker.ui.viewmodel.CategoryViewModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    refreshKey: Int,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val categories = produceState<List<Category>>(initialValue = emptyList(), key1 = refreshKey) {
        val firebaseApp = Firebase.app
        val db = Firebase.firestore(firebaseApp, "financetracker")
        value = db.collection("categories").get().await().toObjects(Category::class.java)
    }
//    val categories by categoryViewModel.categories.collectAsState()
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val currentDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd"))
    }
    var date by remember { mutableStateOf(currentDate) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Add New Transaction", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") }
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") }
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { selectedCategory = it },
                        label = { Text("Category") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.value.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.name
                                    expanded = false
                                }
                            )
                        }

                    }

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.padding(8.dp))
                    Button(onClick = {
                        val newTransaction = Transaction(
                            description = description,
                            amount = amount.toDoubleOrNull() ?: 0.0, // Safely convert to Double
                            date = date,
                            type = selectedCategory
                        )
                        onConfirm(newTransaction)
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
