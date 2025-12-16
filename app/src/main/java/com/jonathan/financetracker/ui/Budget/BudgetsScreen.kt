package com.jonathan.financetracker.ui.Budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.addBudgetToFirestore
import com.jonathan.financetracker.ui.addBudget.AddBudgetDialog
import com.jonathan.financetracker.ui.components.BudgetItem
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