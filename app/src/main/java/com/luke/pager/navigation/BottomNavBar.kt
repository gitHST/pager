package com.luke.pager.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        // NavItem("activity", "Activity"),
        NavItem("diary", "Diary"),
        NavItem("plus", "+"),
        // NavItem("search", "Search"),
        // NavItem("quotes", "Quotes")
    )

    NavigationBar {
        val currentRoute by navController.currentBackStackEntryAsState()
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.destination?.route == item.route,
                onClick = { navController.navigate(item.route) },
                icon = {},
                label = { Text(item.label) }
            )
        }
    }
}

data class NavItem(val route: String, val label: String)