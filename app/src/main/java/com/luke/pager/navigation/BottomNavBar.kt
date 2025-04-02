package com.luke.pager.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        NavItem("diary", "Diary"),
        NavItem("search", "Search"),
        NavItem("plus", "+"),
        NavItem("quotes", "Quotes"),
        NavItem("library", "Library")
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