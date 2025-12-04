package com.luke.pager.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
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
import com.luke.pager.data.viewmodel.QuoteUiStateViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.ActivityScreen
import com.luke.pager.screens.DiaryScreen
import com.luke.pager.screens.ExploreScreen
import com.luke.pager.screens.ReviewScreen
import com.luke.pager.screens.addscreen.SearchAndResultsModal
import com.luke.pager.screens.quotescreen.quotelist.QuotesScreen
import com.luke.pager.screens.quotescreen.scan.MultiPagePreviewModal
import com.luke.pager.screens.quotescreen.scan.ScanScreen
import com.luke.pager.screens.quotescreen.scan.takePhotoHandler
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PagerNavHost(
    navController: NavHostController,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel,
) {
    val topLevelRoutes = listOf("diary", "plus", "quotes")

    val currentRouteEntry by navController.currentBackStackEntryAsState()

    val uiStateViewModel: QuoteUiStateViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ViewModelStoreOwner
    )

    val previousRoute = remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val testMode = false

    val photoLauncher = takePhotoHandler(
        snackbarScope = coroutineScope,
        onPhotoCaptured = { photoUri ->
            uiStateViewModel.setCapturedImageUri(photoUri.toString())
            if (navController.currentDestination?.route != "scan_screen") {
                navController.navigate("scan_screen")
            }
        },
        testMode = testMode
    )

    LaunchedEffect(currentRouteEntry) {
        val newRoute = currentRouteEntry?.destination?.route
        if (previousRoute.value == "quotes" && newRoute != "quotes") {
            delay(500)
            uiStateViewModel.reset()
        }
        previousRoute.value = newRoute
    }

    NavHost(
        navController = navController,
        startDestination = "diary",
        enterTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route

            val fromIndex = topLevelRoutes.indexOf(fromRoute)
            val toIndex = topLevelRoutes.indexOf(toRoute)

            when {
                fromIndex != -1 && toIndex != -1 && toIndex > fromIndex ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 250)
                    )

                fromIndex != -1 && toIndex != -1 && toIndex < fromIndex ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 250)
                    )

                else ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 250)
                    )
            }
        },
        exitTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route

            val fromIndex = topLevelRoutes.indexOf(fromRoute)
            val toIndex = topLevelRoutes.indexOf(toRoute)

            when {
                fromIndex != -1 && toIndex != -1 && toIndex > fromIndex ->
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 250)
                    )

                fromIndex != -1 && toIndex != -1 && toIndex < fromIndex ->
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 250)
                    )

                else ->
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 250)
                    )
            }
        },
        popEnterTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route

            val fromIndex = topLevelRoutes.indexOf(fromRoute)
            val toIndex = topLevelRoutes.indexOf(toRoute)

            when {
                fromIndex != -1 && toIndex != -1 && toIndex > fromIndex ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 250)
                    )

                fromIndex != -1 && toIndex != -1 && toIndex < fromIndex ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 250)
                    )

                else ->
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 250)
                    )
            }
        },
        popExitTransition = {
            val fromRoute = initialState.destination.route
            val toRoute = targetState.destination.route

            val fromIndex = topLevelRoutes.indexOf(fromRoute)
            val toIndex = topLevelRoutes.indexOf(toRoute)

            when {
                fromIndex != -1 && toIndex != -1 && toIndex > fromIndex ->
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 250)
                    )

                fromIndex != -1 && toIndex != -1 && toIndex < fromIndex ->
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 250)
                    )

                else ->
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(durationMillis = 250)
                    )
            }
        }
    ) {
        composable("activity") {
            ActivityScreen(bookViewModel)
        }

        composable("diary") {
            DiaryScreen(navController, bookViewModel)
        }

        composable("plus") {
            SearchAndResultsModal(
                onDismiss = {},
                bookViewModel = bookViewModel,
                navController = navController
            )
        }

        composable("explore") {
            ExploreScreen(bookViewModel)
        }

        composable("quotes") {
            QuotesScreen(
                bookViewModel = bookViewModel,
                quoteViewModel = quoteViewModel,
                uiStateViewModel = uiStateViewModel,
                photoLauncher = photoLauncher
            )
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
                navController = navController,
                debugMode = testMode
            )
        }

        composable("multi_page_preview") {
            MultiPagePreviewModal(
                scannedPages = uiStateViewModel.scannedPages.collectAsState().value,
                uiStateViewModel = uiStateViewModel,
                navController = navController
            )
        }
    }
}
