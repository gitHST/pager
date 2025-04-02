package com.luke.pager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.luke.pager.screens.*

@Composable
fun PagerNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "diary") {
        composable("diary") { DiaryScreen() }
        composable("search") { SearchScreen() }
        composable("plus") { AddBookScreen() }
        composable("quotes") { QuotesScreen() }
        composable("library") { LibraryScreen() }
    }
}