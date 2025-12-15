// In LoginScreen.kt

package com.jonathan.financetracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.Login.LoginViewModel
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Composable
fun LoginScreen(
    openDashboard: () -> Unit, // This lambda will be called on successful login
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val shouldRestartApp by viewModel.shouldRestartApp.collectAsStateWithLifecycle()

    if (shouldRestartApp) {
        openDashboard()
    } else {
        LoginScreenContent(
//            openSignUpScreen = openSignUpScreen,
            signIn = viewModel::signIn,
            showErrorSnackbar = showErrorSnackbar
        )
    }
}




@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoginScreenContent(
//    openSignUpScreen: () -> Unit,
    signIn: (String, String, (ErrorMessage) -> Unit) -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                signIn(email, password, showErrorSnackbar)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
//            coroutineScope.launch {
//                try {
//                    auth.createUserWithEmailAndPassword(email, password).await()
//                    openDashboard() // call the lambda on successful login
//                }
//                catch (e: Exception) {
//                    // Handle errors (e.g., weak password, email already in use)
//                    println("Registration failed: ${e.message}")
//                }
//            }
        }) {
            Text("Don't have an account? Sign Up")
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun SignInScreenPreview() {
    FinanceTrackerTheme {
        LoginScreenContent(
//            openSignUpScreen = {},
            signIn = { _, _, _ -> },
            showErrorSnackbar = {}
        )
    }
}



//@Composable
//fun LoginScreen(
//    openDashboard: () -> Unit, // This lambda will be called on successful login
//    showErrorSnackbar: (ErrorMessage) -> Unit,
//    viewModel: LoginViewModel = hiltViewModel()
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    val auth: FirebaseAuth = Firebase.auth // get the firebaseauth instance
//
//    val coroutineScope = rememberCoroutineScope() // scope for launching async tasks
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Login", style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                coroutineScope.launch {
//                    try {
//                        auth.signInWithEmailAndPassword(email, password).await()
//                        openDashboard() // call the lambda on successful login
//                    }
//                    catch (e: Exception) {
//                        // handle error (shows something)
//                        println("login failed: ${e.message}")
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Login")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        TextButton(onClick = {
//            coroutineScope.launch {
//                try {
//                    auth.createUserWithEmailAndPassword(email, password).await()
//                    openDashboard() // call the lambda on successful login
//                }
//                catch (e: Exception) {
//                    // Handle errors (e.g., weak password, email already in use)
//                    println("Registration failed: ${e.message}")
//                }
//            }
//        }) {
//            Text("Don't have an account? Sign Up")
//        }
//    }
//}
//