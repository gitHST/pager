package com.luke.pager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
            // NavItem("activity", "Activity"),
            NavItem("diary", "Diary", Icons.Filled.Book),
            NavItem("plus", "Add", Icons.Filled.Add),
            // NavItem("explore", "Explore"),
            NavItem("quotes", "Quotes", Icons.Filled.FormatQuote)
        )

    NavigationBar {
        val currentRoute by navController.currentBackStackEntryAsState()
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.destination?.route == item.route,
                onClick = { navController.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) }
            )
        }
    }
}



data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)