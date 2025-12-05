// MainActivity.kt
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
import com.luke.pager.data.AppDatabase
import com.luke.pager.data.repo.BookRepository
import com.luke.pager.data.repo.QuoteRepository
import com.luke.pager.data.repo.ReviewRepository
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.navigation.BottomNavBar
import com.luke.pager.navigation.PagerNavHost
import com.luke.pager.ui.theme.NiceBlue
import com.luke.pager.ui.theme.PagerTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenLayout = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen = screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE || screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE
        val isFoldableOrTablet = isLargeScreen || isDeviceFoldable()

        if (!isFoldableOrTablet) {
            @Suppress("SourceLockedOrientationActivity")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // exportDatabase()
        // restoreDatabase()
        // /data/data/com.luke.pager/files/

        val db =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pager-db")
                .fallbackToDestructiveMigration(true)
                .build()
                .apply {
                    GlobalScope.launch {
                        // comment out this line to keep the database
                        // clearAllTables()
                        // seedDatabaseIfEmpty(this@apply)
                    }
                }
        val bookDao = db.bookDao()
        val reviewDao = db.reviewDao()
        val quoteDao = db.quoteDao()
        val bookRepo = BookRepository(bookDao)
        val reviewRepo = ReviewRepository(reviewDao, bookDao)
        val quoteRepo = QuoteRepository(quoteDao)
        val bookViewModel = BookViewModel(bookRepo, reviewRepo)
        val reviewViewModel = ReviewViewModel(reviewRepo)
        val quoteViewModel = QuoteViewModel(quoteRepo)

        setContent {
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

        // Routes where we want to hide the bottom bar
        val hideBottomBarRoutes = setOf(
            "scan_screen",
            "multi_page_preview",
            "profile" // Settings screen
        )
        val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

        // Single source of truth for transition duration
        val transitionDurationMillis = 200

        var animatedTargetColor by remember { mutableStateOf<Color?>(null) }
        var bottomBarVisible by remember { mutableStateOf(true) }

        // Background color transition
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

        // Bottom bar show/hide is delayed by the same transition duration
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
