// In LoginScreen.kt

package com.jonathan.financetracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit // This lambda will be called on successful login
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth: FirebaseAuth = Firebase.auth // get the firebaseauth instance

    val coroutineScope = rememberCoroutineScope() // scope for launching async tasks

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
                coroutineScope.launch {
                    try {
                        auth.signInWithEmailAndPassword(email, password).await()
                        onLoginSuccess() // call the lambda on successful login
                    }
                    catch (e: Exception) {
                        // handle error (shows something)
                        println("login failed: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            coroutineScope.launch {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    onLoginSuccess() // call the lambda on successful login
                }
                catch (e: Exception) {
                    // Handle errors (e.g., weak password, email already in use)
                    println("Registration failed: ${e.message}")
                }
            }
        }) {
            Text("Don't have an account? Sign Up")
        }
    }
}
    