// MainActivity.kt
package com.luke.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.luke.pager.navigation.BottomNavBar
import com.luke.pager.navigation.PagerNavHost
import com.luke.pager.ui.theme.PagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagerApp()
        }
    }
}

@Composable
fun PagerApp() {
    PagerTheme {
        val navController = rememberNavController()

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavBar(navController) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PagerNavHost(navController)
            }
        }
    }
}
