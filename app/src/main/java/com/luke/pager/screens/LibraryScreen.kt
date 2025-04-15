package com.luke.pager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.luke.pager.data.viewmodel.BookViewModel

@Composable
fun LibraryScreen(viewModel: BookViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Library Screen", fontSize = 24.sp)
    }
}
