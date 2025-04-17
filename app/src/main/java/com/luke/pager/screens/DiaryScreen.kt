package com.luke.pager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.viewmodel.BookViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun DiaryScreen(
    navController: NavController,
    bookViewModel: BookViewModel
) {
    val books by bookViewModel.books.collectAsState()
    val reviews by bookViewModel.allReviews.collectAsState()

    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
        bookViewModel.loadAllReviews()
    }

    val booksWithReviews = books.mapNotNull { book ->
        val review = reviews[book.id]
        if (review?.dateReviewed != null) {
            Pair(book, review)
        } else {
            null
        }
    }

    val sortedBooks = booksWithReviews.sortedByDescending { (_, review) -> review.dateReviewed }
    val groupedBooks = sortedBooks.groupBy { (_, review) -> getDateWithoutTime(review.dateReviewed) }


    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(text = "Diary", fontSize = 24.sp)
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
                groupedBooks.forEach { (date, bookPairs) ->
                    item {
                        Text(
                            text = date,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(bookPairs) { (book, review) ->
                        BookItem(
                            book = book,
                            review = review,
                            onReviewClick = {
                                navController.navigate("review_screen/${book.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BookItem(book: BookEntity, review: ReviewEntity?, onReviewClick: () -> Unit) {
    val trimAmount = 40
    // At some point change this so the text fades to grey instead of being cut off
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, fontSize = 18.sp)
            if (!book.authors.isNullOrBlank()) {
                Text(text = "By ${book.authors}", fontSize = 14.sp)
            }
            val reviewText = review?.reviewText ?: "No review yet"
            val truncatedReview = if (reviewText.length > trimAmount) {
                reviewText.take(trimAmount-3) + "..."
            } else {
                reviewText
            }

            Text(
                text = truncatedReview,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onReviewClick) {
                Text("See Review")
            }
        }
    }
}





fun getDateWithoutTime(dateString: String?): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val reviewDate = inputFormat.parse(dateString ?: "") ?: return "Unknown Date"

        val calendarNow = java.util.Calendar.getInstance()
        val calendarReview = java.util.Calendar.getInstance().apply {
            time = reviewDate
        }

        val yearDifference = calendarNow.get(java.util.Calendar.YEAR) - calendarReview.get(java.util.Calendar.YEAR)
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val month = monthFormat.format(reviewDate)

        if (yearDifference >= 1 || calendarNow.get(java.util.Calendar.DAY_OF_YEAR) < calendarReview.get(java.util.Calendar.DAY_OF_YEAR)) {
            "$month ${calendarReview.get(java.util.Calendar.YEAR)}"
        } else {
            month
        }
    } catch (_: Exception) {
        "Unknown Date"
    }
}


