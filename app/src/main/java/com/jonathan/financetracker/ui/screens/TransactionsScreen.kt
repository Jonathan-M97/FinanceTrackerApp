package com.jonathan.financetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
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
import com.jonathan.financetracker.data.Transaction
import com.jonathan.financetracker.data.addTransactionToFirestore
import com.jonathan.financetracker.ui.components.AddTransactionDialog
import com.jonathan.financetracker.ui.components.TransactionItem
import kotlinx.coroutines.tasks.await

@Composable
fun TransactionsScreen(
    showAddTransactionDialog: Boolean,
    refreshKey: Int,
    onDialogDismiss: () -> Unit,
    onDialogConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {

    var refreshKey by remember { mutableStateOf(0) }
    val transactionsState = produceState<List<Transaction>>(initialValue = emptyList(), key1 = refreshKey) {
        val firebaseApp = Firebase.app
        val db = Firebase.firestore(firebaseApp, "financetracker")
        value = db.collection("transactions").get().await().toObjects(Transaction::class.java)
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            refreshKey = refreshKey,
            onDismiss = onDialogDismiss,
            onConfirm = { newTransaction ->
                addTransactionToFirestore(newTransaction)
                onDialogConfirm()
            }
        )
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        modifier = modifier.padding(horizontal = dimensionResource(R.dimen.padding_small))
    ) {
        items(transactionsState.value) { transaction ->
            TransactionItem(
                transaction = transaction
            )
        }
    }
}