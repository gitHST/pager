package com.luke.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.luke.pager.ui.theme.PagerTheme

// MainActivity sets up the composable content for the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagerApp()
        }
    }
}

// PagerApp is the main composable function that sets up the Scaffold and navigation
@Composable
fun PagerApp() {
    PagerTheme {
        val navController = rememberNavController()

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavBar(navController) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PagerNavHost(navController)
            }
        }
    }
}

// PagerNavHost defines the navigation routes for the app
@Composable
fun PagerNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "diary") {
        composable("diary") { ActivityScreen("Diary Activity") }
        composable("search") { ActivityScreen("Search Activity") }
        composable("book_info") { ActivityScreen("Book Info") }
        composable("review") { ActivityScreen("Review Activity") }
        composable("stats") { ActivityScreen("Stats Activity") }
    }
}

// BottomNavBar sets up the bottom navigation bar with navigation items
@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        NavItem("diary", "Diary"),
        NavItem("search", "Search"),
        NavItem("book_info", "Book Info"),
        NavItem("review", "Review"),
        NavItem("stats", "Stats")
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) },
                icon = {},
                label = { Text(item.label) }
            )
        }
    }
}

// NavItem represents a navigation item with a route and a label
data class NavItem(val route: String, val label: String)

// ActivityScreen displays the name of the activity as text
@Composable
fun ActivityScreen(activityName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = activityName, fontSize = 24.sp, color = Color.White)
    }
}

// SolidColorBackground wraps content with a solid background color
@Composable
fun SolidColorBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181C1F)),  // Replace with your solid color
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
