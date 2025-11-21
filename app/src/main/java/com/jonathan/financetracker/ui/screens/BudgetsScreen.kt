package com.jonathan.financetracker.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.Budget
import com.jonathan.financetracker.ui.components.BudgetItem
import kotlinx.coroutines.tasks.await

@Composable
fun BudgetsScreen(modifier: Modifier = Modifier) {
    val budgetsState = produceState<List<Budget>>(initialValue = emptyList()) {
        val firebaseApp = Firebase.app
        val db = Firebase.firestore(firebaseApp, "financetracker")
        value = db.collection("budgets").get().await().toObjects(Budget::class.java)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        budgetsState.value.forEach { budget ->
            BudgetItem(
                category = budget.category,
                amount = budget.amount.toString(),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(R.dimen.padding_extra_small))
            )
        }
    }
}
