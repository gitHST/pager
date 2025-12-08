package com.luke.pager

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.luke.pager.auth.AuthManager
import com.luke.pager.data.repo.FirebaseBookRepository
import com.luke.pager.data.repo.FirebaseQuoteRepository
import com.luke.pager.data.repo.FirebaseReviewRepository
import com.luke.pager.data.repo.FirebaseUserSettingsRepository
import com.luke.pager.data.repo.IBookRepository
import com.luke.pager.data.repo.IQuoteRepository
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.navigation.BottomNavBar
import com.luke.pager.navigation.PagerNavHost
import com.luke.pager.ui.theme.BackgroundDark
import com.luke.pager.ui.theme.BackgroundLight
import com.luke.pager.ui.theme.LocalUseDarkTheme
import com.luke.pager.ui.theme.PagerTheme
import com.luke.pager.ui.theme.PlusBackgroundDark
import com.luke.pager.ui.theme.PlusBackgroundLight
import com.luke.pager.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenLayout =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen =
            screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE
        val isFoldableOrTablet = isLargeScreen || isDeviceFoldable()

        if (!isFoldableOrTablet) {
            @Suppress("SourceLockedOrientationActivity")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContent {
            var ready by remember { mutableStateOf(false) }
            var uid by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                uid = AuthManager.ensureAnonymousUser()
                ready = true
            }

            if (!ready) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                return@setContent
            }

            val bookRepo: IBookRepository = FirebaseBookRepository(uid)
            val reviewRepo: IReviewRepository = FirebaseReviewRepository(uid)
            val quoteRepo: IQuoteRepository = FirebaseQuoteRepository(uid)
            val settingsRepository = remember { FirebaseUserSettingsRepository(uid) }

            val bookViewModel = remember { BookViewModel(bookRepo, reviewRepo) }
            val reviewViewModel = remember { ReviewViewModel(reviewRepo) }
            val quoteViewModel = remember { QuoteViewModel(quoteRepo) }

            val coroutineScope = rememberCoroutineScope()

            var themeMode by remember { mutableStateOf<ThemeMode?>(null) }

            LaunchedEffect(settingsRepository) {
                settingsRepository.themeModeFlow.collect { mode ->
                    themeMode = mode
                }
            }

            if (themeMode == null) {
                val systemIsDark = isSystemInDarkTheme()
                CompositionLocalProvider(LocalUseDarkTheme provides systemIsDark) {
                    PagerTheme(useDarkTheme = systemIsDark) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(if (systemIsDark) BackgroundDark else BackgroundLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            } else {
                PagerAppUI(
                    bookViewModel = bookViewModel,
                    reviewViewModel = reviewViewModel,
                    quoteViewModel = quoteViewModel,
                    themeMode = themeMode!!,
                    onThemeModeChange = { mode ->
                        themeMode = mode

                        coroutineScope.launch {
                            settingsRepository.setThemeMode(mode)
                        }
                    },
                )
            }
        }
    }

    private fun isDeviceFoldable(): Boolean {
        val config = resources.configuration
        return config.smallestScreenWidthDp >= 600
    }
}

@Composable
fun PagerAppUI(
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()
    val useDarkTheme =
        when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemIsDark
        }

    CompositionLocalProvider(LocalUseDarkTheme provides useDarkTheme) {
        PagerTheme(useDarkTheme = useDarkTheme) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val snackbarHostState = remember { SnackbarHostState() }

            val hideBottomBarRoutes =
                setOf(
                    "scan_screen",
                    "multi_page_preview",
                    "settings",
                )
            val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

            val transitionDurationMillis = 100

            var animatedTargetColor by remember { mutableStateOf<Color?>(null) }
            var bottomBarVisible by remember { mutableStateOf(true) }

            var textureAlpha by remember { mutableFloatStateOf(if (useDarkTheme) 0.1f else 0.9f) }
            var prevUseDarkTheme by remember { mutableStateOf(useDarkTheme) }
            var prevRoute by remember { mutableStateOf(currentRoute) }

            LaunchedEffect(bookViewModel) {
                bookViewModel.loadBooks()
                bookViewModel.loadAllReviews()
                quoteViewModel.loadAllQuotes()
            }

            val isInitialLoading by bookViewModel.isInitialLoading.collectAsState()

            fun backgroundColorFor(
                route: String?,
                dark: Boolean,
            ): Color =
                when (route) {
                    "plus" -> if (dark) PlusBackgroundDark else PlusBackgroundLight
                    else -> if (dark) BackgroundDark else BackgroundLight
                }

            LaunchedEffect(currentRoute, useDarkTheme) {
                if (animatedTargetColor == null) {
                    animatedTargetColor = backgroundColorFor(currentRoute, useDarkTheme)
                    textureAlpha = if (useDarkTheme) 0.1f else 0.9f
                    prevUseDarkTheme = useDarkTheme
                    prevRoute = currentRoute
                    return@LaunchedEffect
                }

                val isThemeChanged = useDarkTheme != prevUseDarkTheme
                val isRouteChanged = currentRoute != prevRoute

                if (isThemeChanged && !prevUseDarkTheme) {
                    textureAlpha = 0.1f
                    delay(transitionDurationMillis.toLong())
                    animatedTargetColor = backgroundColorFor(currentRoute, true)
                } else if (isThemeChanged) {
                    animatedTargetColor = backgroundColorFor(currentRoute, false)
                    delay(transitionDurationMillis.toLong())
                    textureAlpha = 0.9f
                } else if (isRouteChanged) {
                    animatedTargetColor = backgroundColorFor(currentRoute, useDarkTheme)
                }

                prevUseDarkTheme = useDarkTheme
                prevRoute = currentRoute
            }

            LaunchedEffect(shouldShowBottomBar) {
                delay(transitionDurationMillis.toLong())
                bottomBarVisible = shouldShowBottomBar
            }

            val animatedBackgroundColor by animateColorAsState(
                targetValue = animatedTargetColor ?: Color.Transparent,
                animationSpec = tween(durationMillis = transitionDurationMillis),
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(animatedBackgroundColor),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clean_gray_paper),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(textureAlpha),
                )

                Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (bottomBarVisible) {
                            BottomNavBar(navController)
                        }
                    },
                ) { paddingValues ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                    ) {
                        PagerNavHost(
                            navController = navController,
                            bookViewModel = bookViewModel,
                            reviewViewModel = reviewViewModel,
                            quoteViewModel = quoteViewModel,
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                        )
                    }
                }

                if (isInitialLoading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color(0x66000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
