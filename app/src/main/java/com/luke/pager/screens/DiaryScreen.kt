package com.luke.pager.screens

import Privacy
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
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

    val booksWithReviews =
        books.mapNotNull { book ->
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
            modifier =
            Modifier
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
                modifier =
                Modifier
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
fun BookItem(
    book: BookEntity,
    review: ReviewEntity?,
    onReviewClick: () -> Unit
) {
    val trimAmount = 37
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable { onReviewClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxWidth(0.2f)
                    .aspectRatio(0.66f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                var imageBitmap by remember(book.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                var loading by remember(book.id) { mutableStateOf(true) }

                LaunchedEffect(book.cover, book.id) {
                    if (book.cover != null) {
                        loading = true
                        val bitmap = BitmapFactory.decodeByteArray(book.cover, 0, book.cover.size)
                        imageBitmap = bitmap.asImageBitmap()
                        loading = false
                    } else {
                        imageBitmap = null
                        loading = false
                    }
                }

                if (loading && book.cover != null) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = null,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Book,
                                contentDescription = "No Cover Icon",
                                tint = Color.LightGray,
                                modifier =
                                Modifier
                                    .fillMaxWidth(0.5f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No cover",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = book.title, fontSize = 18.sp)
                if (!book.authors.isNullOrBlank()) {
                    Text(
                        text = "By ${book.authors}",
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val reviewText = review?.reviewText?.takeIf { it.isNotBlank() }
                val displayText =
                    reviewText?.let {
                        if (it.length > trimAmount) it.take(trimAmount - 3) + "..." else it
                    } ?: "No review given"

                val isPlaceholder = reviewText == null

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (review != null) {
                        val privacyIcon =
                            when (review.privacy) {
                                Privacy.PUBLIC -> Icons.Filled.Public
                                Privacy.PRIVATE -> Icons.Filled.Lock
                                Privacy.FRIENDS -> Icons.Filled.Group
                            }

                        Icon(
                            imageVector = privacyIcon,
                            contentDescription = "Privacy Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal
                        )
                    )
                }

                if (review?.rating != null) {
                    val rating = review.rating.toFloat()
                    val starSize = 16.dp
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.width(90.dp)) {
                        for (i in 1..5) {
                            val icon =
                                when {
                                    rating >= i -> Icons.Filled.Star
                                    rating == i - 0.5f -> Icons.AutoMirrored.Outlined.StarHalf
                                    else -> Icons.Outlined.StarBorder
                                }
                            Box(
                                modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.height(starSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getDateWithoutTime(dateString: String?): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val reviewDate = inputFormat.parse(dateString ?: "") ?: return "Unknown Date"

        val calendarNow = java.util.Calendar.getInstance()
        val calendarReview =
            java.util.Calendar.getInstance().apply {
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
