package com.luke.pager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.BookViewModel

@Composable
fun AddBookScreen(viewModel: BookViewModel) {
    val books by viewModel.books.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Add Book Screen", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val existingCount = books.count { it.title.startsWith("Book") }
                val newTitle = if (existingCount == 0) "Book1" else "Book${existingCount + 1}"

                val newBook = BookEntity(
                    title = newTitle,
                    authors = "John Doe"
                )

                viewModel.addBook(newBook)
            }) {
                Text("Add Book")
            }
        }
    }
}
