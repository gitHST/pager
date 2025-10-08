package com.luke.pager.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.luke.pager.screens.quotescreen.scan.screens.MultiPagePreviewModal
import com.luke.pager.screens.quotescreen.scan.screens.ScanScreen
import com.luke.pager.screens.quotescreen.scan.takePhotoHandler
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PagerNavHost(
    navController: NavHostController,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel,
    snackbarHostState: SnackbarHostState
) {
    val navItems = listOf(
        // NavItem("activity", "Activity"),
        NavItem("diary", "Diary", Icons.Filled.Book),
        NavItem("plus", "Add", Icons.Filled.Add),
        // NavItem("explore", "Explore"),
        NavItem("quotes", "Quotes", Icons.Filled.FormatQuote)
    )

    val currentRoute by navController.currentBackStackEntryAsState()

    val uiStateViewModel: QuoteUiStateViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ViewModelStoreOwner
    )

    val previousRoute = remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val photoLauncher = takePhotoHandler(
        snackbarScope = coroutineScope,
        onPhotoCaptured = { photoUri ->
            uiStateViewModel.setCapturedImageUri(photoUri.toString())
            navController.navigate("scan_screen")
        },
        testMode = false
    )

    LaunchedEffect(currentRoute) {
        if (previousRoute.value == "quotes" && currentRoute?.destination?.route != "quotes") {
            delay(500)
            uiStateViewModel.reset()
        }
        previousRoute.value = currentRoute?.destination?.route
    }

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
                    navItems = navItems,
                    uiStateViewModel = uiStateViewModel,
                    snackbarHostState = snackbarHostState,
                    photoLauncher = photoLauncher
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

        composable("scan_screen") {
            ScanScreen(
                uiStateViewModel = uiStateViewModel,
                photoLauncher = photoLauncher,
                navController = navController
            )
        }

        composable("multi_page_preview") {
            MultiPagePreviewModal(
                scannedPages = uiStateViewModel.scannedPages.collectAsState().value,
                uiStateViewModel = uiStateViewModel,
                navController = navController,
                onDismiss = {
                    uiStateViewModel.clearScannedPages()
                }
            )
        }
    }
}
