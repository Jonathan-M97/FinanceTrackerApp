package com.jonathan.financetracker

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.ui.Budget.BudgetRoute
import com.jonathan.financetracker.ui.Budget.BudgetsScreen
import com.jonathan.financetracker.ui.Dashboard.DashboardRoute
import com.jonathan.financetracker.ui.Dashboard.DashboardScreen
import com.jonathan.financetracker.ui.ExamplePage
import com.jonathan.financetracker.ui.ExampleScreen
import com.jonathan.financetracker.ui.addBudget.AddBudgetRoute
import com.jonathan.financetracker.ui.addBudget.AddBudgetScreen
import com.jonathan.financetracker.ui.addtransaction.AddTransactionRoute
import com.jonathan.financetracker.ui.addtransaction.AddTransactionScreen
import com.jonathan.financetracker.ui.settings.SettingsRoute
import com.jonathan.financetracker.ui.settings.SettingsScreen
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setSoftInputMode()

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()

            FinanceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = DashboardRoute,
                            modifier = Modifier.padding(innerPadding)
                        ) {

                            composable<ExamplePage> { ExampleScreen(

                            ) }
                            composable<DashboardRoute> { DashboardScreen(
                                openSettingsScreen = {
                                    navController.navigate(SettingsRoute) { launchSingleTop = true }
                                },
                                openBudgetScreen = {
                                    navController.navigate(BudgetRoute) { launchSingleTop = true }
                                },
                                openAddTransactionScreen = { itemId ->
                                    navController.navigate(AddTransactionRoute(itemId)) { launchSingleTop = true }
                                }
                            ) }
                            composable<AddTransactionRoute> { AddTransactionScreen(
                                openDashboard = {
                                    navController.navigate(DashboardRoute) { launchSingleTop = true }
                                },
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                            composable<AddBudgetRoute> { AddBudgetScreen(
                                openDashboard = {
                                    navController.navigate(DashboardRoute) { launchSingleTop = true }
                                },
                                openBudget = {
                                    navController.navigate(BudgetRoute) { launchSingleTop = true }
                                },
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                            composable<BudgetRoute> { BudgetsScreen(
                                openDashboard = {
                                    navController.navigate(DashboardRoute) { launchSingleTop = true }
                                },
                                openSettingsScreen = {
                                    navController.navigate(SettingsRoute) { launchSingleTop = true }
                                },
                                openAddTransactionScreen = { itemId ->
                                    navController.navigate(AddTransactionRoute(itemId)) { launchSingleTop = true }
                                },
                                openAddBudgetScreen = { itemId ->
                                    navController.navigate(AddBudgetRoute(itemId)) { launchSingleTop = true }
                                }
                            ) }
                            composable<SettingsRoute> { SettingsScreen(
                                openDashboard = {
                                    navController.navigate(DashboardRoute) { launchSingleTop = true }
                                },
                                openSignInScreen = {
                                    navController.navigate(LoginRoute) { launchSingleTop = true }
                                }
                            ) }
                            composable<LoginRoute> { LoginScreen(
                                openDashboard = {
                                    navController.navigate(DashboardRoute) { launchSingleTop = true }
                                },
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                        }
                    }
                }
            }
        }
    }


    private fun setSoftInputMode() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun getErrorMessage(error: ErrorMessage): String {
        return when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> this@MainActivity.getString(error.message)
        }
    }
}
