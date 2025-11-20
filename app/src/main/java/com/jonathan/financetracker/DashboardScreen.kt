package com.jonathan.financetracker

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathan.financetracker.data.Transaction
import com.jonathan.financetracker.data.transactions
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.data.Budget
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// Represents the pages the user can navigate to
enum class FinancePage {
    TRANSACTIONS,
    BUDGETS
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppBar(
    currentScreen: String,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = {
            Text(currentScreen,
                color = MaterialTheme.colorScheme.onPrimary
            )
                },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.statusBarsPadding(),
        actions = {
            IconButton(onClick = onSignOutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
fun FinanceTrackerApp(
    onSignOut: () -> Unit
) {

    // state to control the visibility of our dialog? for the add transaction button
    var showDialog by remember { mutableStateOf(false) }

    // This key will be used to force a refresh of the transaction list
    var refreshKey by remember { mutableStateOf(0) }

    // State to manage which page is currently visible
    var currentPage by remember {mutableStateOf(FinancePage.TRANSACTIONS)}


    // This state will hold the list of transactions fetched from Firestore.
    // It starts with an empty list and will be updated once data is loaded.
    val transactionsState = produceState<List<Transaction>>(initialValue = emptyList(), key1 = refreshKey) {
        val firebaseApp = Firebase.app
        // Get a reference to the Firestore database
        val db = Firebase.firestore(firebaseApp, "financetracker")
        // Fetch all documents from the "transactions" collection
        value = db.collection("transactions").get().await().toObjects(Transaction::class.java)
    }

    // Fetch budgets
    val budgetsState = produceState<List<Budget>>(initialValue = emptyList()) {
        // No refreshKey needed unless you plan to add budgets from the app
        val firebaseApp = Firebase.app
        val db = Firebase.firestore(firebaseApp, "financetracker")
        // Fetch from the new "budgets" collection
        value = db.collection("budgets").get().await().toObjects(Budget::class.java)
    }

    Scaffold(
        topBar = {
            FinanceAppBar(
                currentScreen = "Finance Tracker",
                onSignOutClick = onSignOut
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, "Add Transaction")
            }
        }
    ) { innerPadding ->
        // Condition to show dialog when showdialoge is true (for adding transaction)
        if (showDialog) {
            AddTransactionDialog(
                onDismiss = { showDialog = false },
                onConfirm = { newTransaction ->
                    addTransactionToFirestore(newTransaction)
                    // increment the key to trigger refresh
                    refreshKey++
                    showDialog = false
                }
            )
        }
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = dimensionResource(R.dimen.padding_small))
        ) {
            item {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Button to navigate to Transactions page
                    Button(onClick = { currentPage = FinancePage.TRANSACTIONS }) {
                        Text("Transactions")
                    }
                    // Button to navigate to Goals page
                    Button(onClick = { currentPage = FinancePage.BUDGETS }) {
                        Text("Goals")
                    }
                }
            }

            when (currentPage) {
                FinancePage.TRANSACTIONS -> {
                    // Use the data from our new state holder
                    items(transactionsState.value) { transaction ->
                        TransactionItem(
                            transaction = transaction
                        )
                    }
                }


                FinancePage.BUDGETS -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(dimensionResource(R.dimen.padding_small)),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Loop through the budgets fetched from Firestore
                            budgetsState.value.forEach { budget ->
//                                val category = budget.category
//                                val amount = budget.amount.toString()
                                DetailCard(
                                    category = budget.category,
                                    amount = budget.amount.toString(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = dimensionResource(R.dimen.padding_extra_small))
                                )
//                            DetailCard(
//                                "Groceries", "1.00", modifier = Modifier
//                                    .weight(1f)
//                                    .padding(end = dimensionResource(R.dimen.padding_extra_small))
//                            )
//                            DetailCard(
//                                "Mortgage", "1.00", modifier = Modifier
//                                    .weight(1f)
//                                    .padding(end = dimensionResource(R.dimen.padding_extra_small))
//                            )
//                            DetailCard(
//                                "Income", "1.00", modifier = Modifier
//                                    .weight(1f)
//                                    .padding(end = dimensionResource(R.dimen.padding_extra_small))
//                            )
                            }
                        }

                    }
                }
            }

        }
    }
}

@Composable
fun DetailCard(
    category: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)

    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = amount,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Text (
                    text = transaction.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text (
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text (
                    text = transaction.amount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

            }
        }
    }
}


@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    // State for each input field
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

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
                    label = { Text("Date (e.g., Nov 20)") }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") }
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
                        val newTransaction = Transaction(
                            description = description,
                            amount = amount.toDoubleOrNull() ?: 0.0, // Safely convert to Double
                            date = date,
                            type = category
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


@Preview (showBackground = true)
@Composable
fun TransactionItemPreview() {
    FinanceTrackerTheme {
        TransactionItem(
            Transaction("Groceries", 12.43,"Feb 24","Groceries")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailCardPreview() {
    FinanceTrackerTheme {
        DetailCard("Groceries", "1.00", modifier = Modifier.padding(8.dp))
    }
}

//@Preview(showBackground = true)
//@Composable
//fun FinanceTrackerAppPreview() {
//    FinanceTrackerTheme {
//        FinanceTrackerApp(
//
//        )
//    }
//}

fun addTransactionToFirestore(transaction: Transaction) {
    val firebaseApp = Firebase.app
    val db = Firebase.firestore(firebaseApp, "financetracker")
    val currentUser = Firebase.auth.currentUser

    // We need a logged-in user to add data
    if (currentUser != null) {
        // Adding a new document with an auto-generated ID
        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                println("Transaction successfully added!")
            }
            .addOnFailureListener { e ->
                println("Error adding transaction: $e")
            }
    }
}