package com.luke.pager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.viewmodel.BookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(bookViewModel: BookViewModel) {

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(text = "Diary", fontSize = 24.sp)
        }

        val books by bookViewModel.books.collectAsState()
        val sortedBooks = books.sortedByDescending { it.dateAdded }

        // Group books by their date (ignoring the time)
        val groupedBooks = sortedBooks.groupBy { getDateWithoutTime(it.dateAdded) }

        LaunchedEffect(Unit) {
            bookViewModel.loadBooks()
        }

        if (books.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No books yet", fontSize = 20.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedBooks.forEach { (date, books) ->
                    // Group header (date)
                    item {
                        Text(
                            text = date,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    // Books under that date
                    items(books) { book ->
                        BookItem(book.title, book.authors)
                    }
                }
            }
        }
    }
}

// Helper function to extract date without time
fun getDateWithoutTime(dateString: String?): String {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = formatter.parse(dateString ?: "") ?: Date()
        val dateOnlyFormat = SimpleDateFormat("MMMM", Locale.getDefault())  // "13th March"
        dateOnlyFormat.format(date)
    } catch (e: Exception) {
        "Unknown Date"
    }
}

@Composable
// BookItem composable displays book title and author
fun BookItem(title: String, authors: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp)
            if (!authors.isNullOrBlank()) {
                Text(text = "By $authors", fontSize = 14.sp)
            }
        }
    }
}
