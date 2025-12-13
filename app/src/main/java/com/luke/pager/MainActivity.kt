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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.luke.pager.data.repo.FirebaseBookRepository
import com.luke.pager.data.repo.FirebaseQuoteRepository
import com.luke.pager.data.repo.FirebaseReviewRepository
import com.luke.pager.data.repo.FirebaseUserSettingsRepository
import com.luke.pager.data.repo.IBookRepository
import com.luke.pager.data.repo.IQuoteRepository
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.navigation.BottomNavBar
import com.luke.pager.navigation.PagerNavHost
import com.luke.pager.screens.auth.LoggedOutGate
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
        installSplashScreen()
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
            val auth = remember { FirebaseAuth.getInstance() }
            val authViewModel: AuthViewModel = viewModel()

            var uid by remember { mutableStateOf(auth.currentUser?.uid) }

            LaunchedEffect(auth) {
                auth.addAuthStateListener { firebaseAuth ->
                    uid = firebaseAuth.currentUser?.uid
                }
            }

            when (val currentUid = uid) {
                null -> {
                    LoggedOutGate(authViewModel = authViewModel)
                }

                else -> {
                    val bookRepo: IBookRepository =
                        remember(currentUid) { FirebaseBookRepository(currentUid) }
                    val reviewRepo: IReviewRepository =
                        remember(currentUid) { FirebaseReviewRepository(currentUid) }
                    val quoteRepo: IQuoteRepository =
                        remember(currentUid) { FirebaseQuoteRepository(currentUid) }
                    val settingsRepository =
                        remember(currentUid) { FirebaseUserSettingsRepository(currentUid) }

                    val bookViewModel =
                        remember(currentUid) {
                            com.luke.pager.data.viewmodel
                                .BookViewModel(bookRepo, reviewRepo)
                        }
                    val reviewViewModel =
                        remember(currentUid) {
                            com.luke.pager.data.viewmodel
                                .ReviewViewModel(reviewRepo)
                        }
                    val quoteViewModel =
                        remember(currentUid) {
                            com.luke.pager.data.viewmodel
                                .QuoteViewModel(quoteRepo)
                        }

                    val coroutineScope = rememberCoroutineScope()

                    var themeMode by remember { mutableStateOf<ThemeMode?>(null) }
                    var syncOverCellular by remember { mutableStateOf(false) }

                    LaunchedEffect(settingsRepository) {
                        settingsRepository.themeModeFlow.collect { result ->
                            themeMode = result.getOrNull() ?: ThemeMode.SYSTEM
                        }
                    }

                    LaunchedEffect(settingsRepository) {
                        settingsRepository.syncOverCellularFlow.collect { result ->
                            syncOverCellular = result.getOrNull() ?: false
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
                                            .background(
                                                if (systemIsDark) {
                                                    BackgroundDark
                                                } else {
                                                    BackgroundLight
                                                },
                                            ),
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
                            syncOverCellular = syncOverCellular,
                            onSyncOverCellularChange = { enabled ->
                                syncOverCellular = enabled
                                coroutineScope.launch {
                                    settingsRepository.setSyncOverCellular(enabled)
                                }
                            },
                        )
                    }
                }
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
    bookViewModel: com.luke.pager.data.viewmodel.BookViewModel,
    reviewViewModel: com.luke.pager.data.viewmodel.ReviewViewModel,
    quoteViewModel: com.luke.pager.data.viewmodel.QuoteViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    syncOverCellular: Boolean,
    onSyncOverCellularChange: (Boolean) -> Unit,
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
            val snackbarScope = rememberCoroutineScope()

            // ✅ Drop snackbar if one is already visible
            val onShowSnackbarOnce: (String) -> Unit = { msg ->
                snackbarScope.launch {
                    if (snackbarHostState.currentSnackbarData != null) return@launch
                    snackbarHostState.showSnackbar(msg)
                }
            }

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

            var textureAlpha by remember {
                mutableFloatStateOf(if (useDarkTheme) 0.1f else 0.9f)
            }
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
                            syncOverCellular = syncOverCellular,
                            onSyncOverCellularChange = onSyncOverCellularChange,
                            onShowSnackbar = onShowSnackbarOnce,
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
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
