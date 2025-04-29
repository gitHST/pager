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
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.ActivityScreen
import com.luke.pager.screens.DiaryScreen
import com.luke.pager.screens.ExploreScreen
import com.luke.pager.screens.ReviewScreen
import com.luke.pager.screens.addscreen.SearchAndResultsModal
import com.luke.pager.screens.quotescreen.QuotesScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PagerNavHost(
    navController: NavHostController,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel
) {
    val navItems = listOf(
        NavItem("diary", "Diary"),
        NavItem("plus", "+"),
        NavItem("quotes", "Quotes")
    )

    val currentRoute by navController.currentBackStackEntryAsState()

    NavHost(
        navController = navController,
        startDestination = "diary"
    ) {
        composable("activity") {
            SwipeToNavigate(
                navController = navController,
                currentRoute = currentRoute?.destination?.route.orEmpty(),
                navItems = navItems
            ) { _, _ ->
                ActivityScreen(bookViewModel)
            }
        }

        composable("diary") {
            SwipeToNavigate(
                navController = navController,
                currentRoute = currentRoute?.destination?.route.orEmpty(),
                navItems = navItems
            ) { _, _ ->
                DiaryScreen(navController, bookViewModel)
            }
        }

        composable("plus") {
            SwipeToNavigate(
                navController = navController,
                currentRoute = currentRoute?.destination?.route.orEmpty(),
                navItems = navItems
            ) { _, _ ->
                SearchAndResultsModal(
                    onDismiss = {},
                    bookViewModel = bookViewModel,
                    navController = navController
                )
            }
        }

        composable("explore") {
            SwipeToNavigate(
                navController = navController,
                currentRoute = currentRoute?.destination?.route.orEmpty(),
                navItems = navItems
            ) { _, _ ->
                ExploreScreen(bookViewModel)
            }
        }

        composable("quotes") {
            SwipeToNavigate(
                navController = navController,
                currentRoute = currentRoute?.destination?.route.orEmpty(),
                navItems = navItems
            ) { currentRoute, navItems ->
                QuotesScreen(
                    bookViewModel = bookViewModel,
                    quoteViewModel = quoteViewModel,
                    navController = navController,
                    currentRoute = currentRoute,
                    navItems = navItems
                )
            }
        }

        composable("review_screen/{reviewId}") { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getString("reviewId")?.toLongOrNull() ?: 0L
            val reviews by bookViewModel.allReviews.collectAsState()
            ReviewScreen(
                reviewId = reviewId,
                reviews = reviews,
                reviewViewModel = reviewViewModel,
                onDeleteSuccess = { navController.popBackStack() }
            )
        }
    }
}
