package com.luke.pager.screens.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.screens.components.NoBooksYetMessage
import com.luke.pager.screens.components.Title
import com.luke.pager.ui.theme.DiaryLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DiaryScreen(
    navController: NavController,
    bookViewModel: BookViewModel,
    diaryLayout: DiaryLayout,
) {
    val books by bookViewModel.books.collectAsState()
    val reviewsById by bookViewModel.allReviews.collectAsState()

    LaunchedEffect(Unit) {
        bookViewModel.loadBooks()
        bookViewModel.loadAllReviews()
    }

    val reviewsByBookId: Map<String, ReviewEntity> =
        reviewsById
            .values
            .filterNotNull()
            .associateBy { it.bookId }

    val booksWithReviews =
        books.mapNotNull { book ->
            val review = reviewsByBookId[book.id]
            if (review?.dateReviewed != null) {
                Pair(book, review)
            } else {
                null
            }
        }

    val sortedBooks = booksWithReviews.sortedByDescending { (_, review) -> review.dateReviewed }
    val groupedBooks = sortedBooks.groupBy { (_, review) -> getDateWithoutTime(review.dateReviewed) }

    Column(modifier = Modifier.fillMaxSize()) {
        Title("Diary")
        if (books.isEmpty()) {
            NoBooksYetMessage()
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                groupedBooks.forEach { (date, bookPairs) ->
                    item {
                        Text(
                            text = date,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    items(bookPairs) { (book, review) ->
                        if (diaryLayout == DiaryLayout.EXPANDED) {
                            BookItemExpanded(
                                book = book,
                                review = review,
                                onReviewClick = {
                                    navController.navigate("review_screen/${review.id}")
                                },
                            )
                        } else {
                            BookItemCompact(
                                book = book,
                                review = review,
                                onReviewClick = {
                                    navController.navigate("review_screen/${review.id}")
                                },
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

fun getDateWithoutTime(dateString: String?): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val reviewDate = inputFormat.parse(dateString ?: "") ?: return "Unknown Date"

        val calendarNow = Calendar.getInstance()
        val calendarReview =
            Calendar.getInstance().apply {
                time = reviewDate
            }

        val yearDifference = calendarNow.get(Calendar.YEAR) - calendarReview.get(Calendar.YEAR)
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val month = monthFormat.format(reviewDate)

        if (yearDifference >= 1 ||
            calendarNow.get(Calendar.DAY_OF_YEAR) < calendarReview.get(Calendar.DAY_OF_YEAR)
        ) {
            "$month ${calendarReview.get(Calendar.YEAR)}"
        } else {
            month
        }
    } catch (_: Exception) {
        "Unknown Date"
    }
}
