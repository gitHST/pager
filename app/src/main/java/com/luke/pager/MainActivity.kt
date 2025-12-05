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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.luke.pager.auth.AuthManager
import com.luke.pager.data.AppDatabase
import com.luke.pager.data.repo.BookRepository
import com.luke.pager.data.repo.FirebaseBookRepository
import com.luke.pager.data.repo.FirebaseQuoteRepository
import com.luke.pager.data.repo.FirebaseReviewRepository
import com.luke.pager.data.repo.IBookRepository
import com.luke.pager.data.repo.IQuoteRepository
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.data.repo.QuoteRepository
import com.luke.pager.data.repo.ReviewRepository
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.navigation.BottomNavBar
import com.luke.pager.navigation.PagerNavHost
import com.luke.pager.ui.theme.NiceBlue
import com.luke.pager.ui.theme.PagerTheme
import kotlinx.coroutines.delay
import java.io.File

class MainActivity : ComponentActivity() {

    private companion object {
        private const val USE_FIREBASE = true
    }

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

            // 🔥 AUTHENTICATION FIRST — ensures anonymous login before loading UI
            LaunchedEffect(Unit) {
                uid = AuthManager.ensureAnonymousUser()
                ready = true
            }

            if (!ready) {
                // Simple loading screen
                Box(Modifier.fillMaxSize().background(Color.White)) {}
                return@setContent
            }

            // Now that we have UID, create repositories
            val bookRepo: IBookRepository
            val reviewRepo: IReviewRepository
            val quoteRepo: IQuoteRepository

            if (USE_FIREBASE) {
                bookRepo = FirebaseBookRepository(uid)     // 🔥 Modified in Step 2
                reviewRepo = FirebaseReviewRepository(uid) // 🔥 Modified in Step 2
                quoteRepo = FirebaseQuoteRepository(uid)   // 🔥 Modified in Step 2
            } else {
                restoreDatabase()

                val db =
                    Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pager-db")
                        .fallbackToDestructiveMigration(true)
                        .build()

                val bookDao = db.bookDao()
                val reviewDao = db.reviewDao()
                val quoteDao = db.quoteDao()

                bookRepo = BookRepository(bookDao)
                reviewRepo = ReviewRepository(reviewDao, bookDao)
                quoteRepo = QuoteRepository(quoteDao)
            }

            val bookViewModel = BookViewModel(bookRepo, reviewRepo)
            val reviewViewModel = ReviewViewModel(reviewRepo)
            val quoteViewModel = QuoteViewModel(quoteRepo)

            PagerAppUI(bookViewModel, reviewViewModel, quoteViewModel)
        }
    }

    private fun isDeviceFoldable(): Boolean {
        val config = resources.configuration
        return config.smallestScreenWidthDp >= 600
    }

    @Suppress("unused")
    fun exportDatabase() {
        val dbFile = applicationContext.getDatabasePath("pager-db")
        val walFile = File(dbFile.parent, "pager-db-wal")
        val shmFile = File(dbFile.parent, "pager-db-shm")

        val backupDir = applicationContext.filesDir
        val backupDb = File(backupDir, "pager-db-backup.db")
        val backupWal = File(backupDir, "pager-db-backup.db-wal")
        val backupShm = File(backupDir, "pager-db-backup.db-shm")

        dbFile.copyTo(backupDb, overwrite = true)
        if (walFile.exists()) walFile.copyTo(backupWal, overwrite = true)
        if (shmFile.exists()) shmFile.copyTo(backupShm, overwrite = true)

        println("✅ Full database export complete: ${backupDb.absolutePath}")
    }

    @Suppress("unused")
    fun restoreDatabase() {
        val dbFile = applicationContext.getDatabasePath("pager-db")
        val walFile = File(dbFile.parent, "pager-db-wal")
        val shmFile = File(dbFile.parent, "pager-db-shm")

        val backupDir = applicationContext.filesDir
        val backupDb = File(backupDir, "pager-db-backup.db")
        val backupWal = File(backupDir, "pager-db-backup.db-wal")
        val backupShm = File(backupDir, "pager-db-backup.db-shm")

        if (backupDb.exists()) {
            backupDb.copyTo(dbFile, overwrite = true)
            if (backupWal.exists()) backupWal.copyTo(walFile, overwrite = true)
            if (backupShm.exists()) backupShm.copyTo(shmFile, overwrite = true)
            println("✅ Full database restore complete: ${backupDb.absolutePath}")
        } else {
            println("❌ Backup database file not found.")
        }
    }
}

@Composable
fun PagerAppUI(
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel
) {
    PagerTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val snackbarHostState = remember { SnackbarHostState() }

        val hideBottomBarRoutes = setOf(
            "scan_screen",
            "multi_page_preview",
            "profile"
        )
        val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

        val transitionDurationMillis = 200

        var animatedTargetColor by remember { mutableStateOf<Color?>(null) }
        var bottomBarVisible by remember { mutableStateOf(true) }

        LaunchedEffect(currentRoute) {
            if (animatedTargetColor == null) {
                delay(1)
            }
            animatedTargetColor =
                when (currentRoute) {
                    "plus" -> NiceBlue
                    else -> Color(0xFFF7FEFF)
                }
        }

        LaunchedEffect(shouldShowBottomBar) {
            delay(transitionDurationMillis.toLong())
            bottomBarVisible = shouldShowBottomBar
        }

        val animatedBackgroundColor by animateColorAsState(
            targetValue = animatedTargetColor ?: Color.Transparent,
            animationSpec = tween(durationMillis = transitionDurationMillis)
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(animatedBackgroundColor)
        ) {
            Image(
                painter = painterResource(id = R.drawable.clean_gray_paper),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .alpha(0.9f)
            )

            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (bottomBarVisible) {
                        BottomNavBar(navController)
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                ) {
                    PagerNavHost(
                        navController,
                        bookViewModel,
                        reviewViewModel,
                        quoteViewModel
                    )
                }
            }
        }
    }
}
