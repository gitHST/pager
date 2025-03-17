package com.luke.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.luke.pager.ui.theme.PagerTheme

class ReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PagerTheme {
                ActivityScreen("Review Activity")
            }
        }
    }
}
