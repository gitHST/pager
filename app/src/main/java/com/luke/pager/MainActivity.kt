// MainActivity.kt
package com.luke.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.luke.pager.data.AppDatabase
import com.luke.pager.data.repo.BookRepository
import com.luke.pager.data.repo.ReviewRepository
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.navigation.BottomNavBar
import com.luke.pager.navigation.PagerNavHost
import com.luke.pager.ui.theme.PagerTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pager-db")
            .fallbackToDestructiveMigration()
            .build()
            .apply {
                GlobalScope.launch {
                    // 👇 comment out this line to keep the database
                    // clearAllTables()
                }

            }
        val bookDao = db.bookDao()
        val reviewDao = db.reviewDao()
        val bookRepo = BookRepository(bookDao)
        val reviewRepo = ReviewRepository(reviewDao)
        val bookViewModel = BookViewModel(bookRepo)
        val reviewViewModel = ReviewViewModel(reviewRepo)

        setContent {
            PagerAppUI(bookViewModel, reviewViewModel)
        }
    }
}


@Composable
fun PagerAppUI(bookViewModel: BookViewModel, reviewViewModel: ReviewViewModel) {
    PagerTheme {
        val navController = rememberNavController()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7FEFF)) // Light cyan background
        ) {
            Image(
                painter = painterResource(id = R.drawable.clean_gray_paper), // Save this image in res/drawable
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.9f) // Adjust visibility of texture
            )

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomNavBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    PagerNavHost(navController, bookViewModel, reviewViewModel)
                }
            }
        }

    }
}

