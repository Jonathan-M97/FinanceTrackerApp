package com.jonathan.financetracker.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.Budget
import com.jonathan.financetracker.data.Transaction
import com.jonathan.financetracker.data.addBudgetToFirestore
import com.jonathan.financetracker.data.addTransactionToFirestore
import com.jonathan.financetracker.ui.components.AddBudgetDialog
import com.jonathan.financetracker.ui.components.BudgetItem
import com.jonathan.financetracker.ui.components.TransactionItem
import kotlinx.coroutines.tasks.await

@Composable
fun BudgetsScreen(
    showAddBudgetDialog: Boolean,
    refreshKey: Int,
    onDialogDismiss: () -> Unit,
    onDialogConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
//    var showDialog by remember { mutableStateOf(false) }
//    var budgets by remember { mutableStateOf<List<Budget>>(emptyList()) }

    val budgetState = produceState<List<Budget>>(initialValue = emptyList(), key1 = refreshKey) {
        val firebaseApp = Firebase.app
        val db = Firebase.firestore(firebaseApp, "financetracker")
        value = db.collection("budgets").get().await().toObjects(Budget::class.java)
    }

//    DisposableEffect(Unit) {
//        val firebaseApp = Firebase.app
//        val db = Firebase.firestore(firebaseApp, "financetracker")
//        val listenerRegistration = db.collection("budget")
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    // Handle error
//                    return@addSnapshotListener
//                }
//                if (snapshot != null) {
//                    budgets = snapshot.toObjects(Budget::class.java)
//                }
//            }
//        onDispose {
//            listenerRegistration.remove()
//        }
//    }


    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = onDialogDismiss,
            onConfirm = { newBudget ->
                addBudgetToFirestore(newBudget)
                onDialogConfirm()
            }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        modifier = modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
    ) {
        items(budgetState.value) { budget ->
            BudgetItem(
                budget = budget
            )
        }
    }
}