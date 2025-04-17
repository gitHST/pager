package com.luke.pager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.ActivityScreen
import com.luke.pager.screens.AddScreen
import com.luke.pager.screens.DiaryScreen
import com.luke.pager.screens.QuotesScreen
import com.luke.pager.screens.ReviewScreen
import com.luke.pager.screens.SearchScreen

@Composable
fun PagerNavHost(
    navController: NavHostController,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel
) {
    NavHost(navController, startDestination = "activity") {
        composable("activity") { ActivityScreen(bookViewModel) }
        composable("diary") { DiaryScreen(navController, bookViewModel) }
        composable("plus") { AddScreen(bookViewModel, reviewViewModel) }
        composable("search") { SearchScreen(bookViewModel) }
        composable("quotes") { QuotesScreen(bookViewModel) }

        // Add composable for Review Screen
        composable("review_screen/{reviewId}") { backStackEntry ->
            val reviewId = backStackEntry.arguments?.getString("reviewId")?.toLongOrNull() ?: 0L
            val reviews by bookViewModel.allReviews.collectAsState()
            ReviewScreen(navController = navController, reviewId = reviewId, reviews = reviews)
        }
    }
}
