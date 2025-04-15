package com.luke.pager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.screens.AddBookScreen
import com.luke.pager.screens.DiaryScreen
import com.luke.pager.screens.LibraryScreen
import com.luke.pager.screens.QuotesScreen
import com.luke.pager.screens.SearchScreen

@Composable
fun PagerNavHost(navController: NavHostController, viewModel: BookViewModel) {
    NavHost(navController, startDestination = "diary") {
        composable("diary") { DiaryScreen(viewModel) }
        composable("search") { SearchScreen(viewModel) }
        composable("plus") { AddBookScreen(viewModel) }
        composable("quotes") { QuotesScreen(viewModel) }
        composable("library") { LibraryScreen(viewModel) }
    }
}