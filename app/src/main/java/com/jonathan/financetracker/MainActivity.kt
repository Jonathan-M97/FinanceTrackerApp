package com.jonathan.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTrackerTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    // Check the current authentication state
    val auth = Firebase.auth
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    if (isLoggedIn) {
        // If logged in, show the main finance tracker app
        FinanceTrackerApp(
            onSignOut = {
                auth.signOut()  // sign the user out from firebase
                isLoggedIn = false  // update our state to re-compose
            }
        )
    } else {
        // If not logged in, show the login screen
        // When onLoginSuccess is called, we update our state to re-compose
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    }
}