package com.luke.pager.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.screens.ActivityScreen
import com.luke.pager.screens.AddScreen
import com.luke.pager.screens.DiaryScreen
import com.luke.pager.screens.QuotesScreen
import com.luke.pager.screens.ReviewScreen
import com.luke.pager.screens.SearchScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PagerNavHost(
    navController: NavHostController,
    bookViewModel: BookViewModel,
) {
    val navItems = listOf(
        NavItem("activity", "Activity"),
        NavItem("diary", "Diary"),
        NavItem("plus", "+"),
        NavItem("search", "Search"),
        NavItem("quotes", "Quotes")
    )

    val currentRoute by navController.currentBackStackEntryAsState()

    NavHost(
        navController = navController,
        startDestination = "plus"
    ) {
        composable("activity") {
            SwipeToNavigate(navController, currentRoute?.destination?.route.orEmpty(), navItems) {
                ActivityScreen(bookViewModel)
            }
        }
        composable("diary") {
            SwipeToNavigate(navController, currentRoute?.destination?.route.orEmpty(), navItems) {
                DiaryScreen(navController, bookViewModel)
            }
        }
        composable("plus") {
            SwipeToNavigate(navController, currentRoute?.destination?.route.orEmpty(), navItems) {
                AddScreen(bookViewModel)
            }
        }
        composable("search") {
            SwipeToNavigate(navController, currentRoute?.destination?.route.orEmpty(), navItems) {
                SearchScreen(bookViewModel)
            }
        }
        composable("quotes") {
            SwipeToNavigate(navController, currentRoute?.destination?.route.orEmpty(), navItems) {
                QuotesScreen(bookViewModel)
            }
        }
        composable("review_screen/{reviewId}") { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getString("reviewId")?.toLongOrNull() ?: 0L
            val reviews by bookViewModel.allReviews.collectAsState()
            ReviewScreen(reviewId = reviewId, reviews = reviews)
        }
    }
}
