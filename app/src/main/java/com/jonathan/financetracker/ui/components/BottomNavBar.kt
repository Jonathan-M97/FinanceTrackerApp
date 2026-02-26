package com.jonathan.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jonathan.financetracker.ui.settings.SettingsRoute
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.jonathan.financetracker.ui.Budget.BudgetRoute
import com.jonathan.financetracker.ui.Dashboard.DashboardRoute

data class BottomNavItem<T : Any>(
    val label: String,
    val icon: ImageVector,
    val route: T
)

@Composable
fun BottomNavBar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, DashboardRoute),
        // BottomNavItem("Transactions", Icons.Default.List, TransactionsRoute),
        BottomNavItem("Budgets", Icons.Default.AttachMoney, BudgetRoute),
        BottomNavItem("Settings", Icons.Default.Settings, SettingsRoute)
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(56.dp)
            .background(NavigationBarDefaults.containerColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = when (item.route) {
                is DashboardRoute -> currentDestination?.hasRoute<DashboardRoute>() == true
                is BudgetRoute -> currentDestination?.hasRoute<BudgetRoute>() == true
                is SettingsRoute -> currentDestination?.hasRoute<SettingsRoute>() == true
                else -> false
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
                        else Color.Transparent
                    )
                    .clickable { onNavigate(item.route) }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(20.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
//    NavigationBar(
//        modifier = Modifier.height(78.dp),
////        windowInsets = WindowInsets(0.dp)
//    ) {
//        items.forEach { item ->
//            NavigationBarItem(
//                selected = when (item.route) {
//                    is DashboardRoute -> currentDestination?.hasRoute<DashboardRoute>() == true
//                    is BudgetRoute -> currentDestination?.hasRoute<BudgetRoute>() == true
//                    else -> false
//                },
//                onClick = { onNavigate(item.route) },
//                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp)) },
//                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
//                alwaysShowLabel = true
//            )
//        }
//    }
//    NavigationBar {
//        items.forEach { item ->
//            NavigationBarItem(
//                selected = when (item.route) {
//                    is DashboardRoute -> currentDestination?.hasRoute<DashboardRoute>() == true
//                    is BudgetRoute -> currentDestination?.hasRoute<BudgetRoute>() == true
//                    is SettingsRoute -> currentDestination?.hasRoute<SettingsRoute>() == true
//                    else -> false
//                },
//                onClick = { onNavigate(item.route) },
//                icon = { Icon(item.icon, contentDescription = item.label) },
//                label = { Text(item.label) }
//            )
//        }
//    }
}