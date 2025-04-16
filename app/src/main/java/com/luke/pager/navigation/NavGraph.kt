package com.luke.pager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.screens.ActivityScreen
import com.luke.pager.screens.AddScreen
import com.luke.pager.screens.DiaryScreen
import com.luke.pager.screens.QuotesScreen
import com.luke.pager.screens.SearchScreen

@Composable
fun PagerNavHost(navController: NavHostController, viewModel: BookViewModel) {
    NavHost(navController, startDestination = "activity") {
        composable("activity") { ActivityScreen(viewModel) }
        composable("diary") { DiaryScreen(viewModel) }
        composable("plus") { AddScreen(viewModel) }
        composable("search") { SearchScreen(viewModel) }
        composable("quotes") { QuotesScreen(viewModel) }
    }
}