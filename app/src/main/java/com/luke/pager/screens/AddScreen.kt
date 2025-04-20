package com.luke.pager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.screens.components.SearchAndResultsModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(bookViewModel: BookViewModel) {
    var showSheet by remember { mutableStateOf(false) }

    // Load books when the screen is first launched
    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
    }

    // Conditionally show the SearchBookModal based on showSheet state
    Box(Modifier.fillMaxSize()) {
        // Your main screen UI
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Add book or review", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showSheet = true }) {
                Text("Add Book")
            }
        }

        // Overlay floating search on top
        if (showSheet) {
            SearchAndResultsModal(onDismiss = { showSheet = false })
        }
    }
}
