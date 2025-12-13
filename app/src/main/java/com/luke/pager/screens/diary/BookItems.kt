package com.luke.pager.screens.diary

import BookCoverImage
import Privacy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity

@Composable
fun BookItemCompact(
    book: BookEntity,
    review: ReviewEntity?,
    onReviewClick: () -> Unit,
) {
    val trimAmount = 37

    val coverUrl =
        if (book.cover == null && book.coverId != null) {
            "https://covers.openlibrary.org/b/id/${book.coverId}-M.jpg"
        } else {
            null
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onReviewClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCoverImage(
                coverData = book.cover,
                coverUrl = coverUrl,
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = book.title, style = MaterialTheme.typography.bodyLarge)
                if (!book.authors.isNullOrBlank()) {
                    Text(
                        text = "By ${book.authors}",
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val reviewText = review?.reviewText?.takeIf { it.isNotBlank() }
                val displayText =
                    reviewText?.let {
                        if (it.length > trimAmount) it.take(trimAmount - 3) + "..." else it
                    } ?: "No review given"

                val isPlaceholder = reviewText == null

                Row(
                    verticalAlignment = Alignment.CenterVertically,
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
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = displayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
                            ),
                    )
                }

                if (review?.rating != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    StarsRow(
                        rating = review.rating,
                        starSize = 16.dp,
                        rowWidth = 90.dp,
                    )
                }
            }
        }
    }
}

@Composable
fun BookItemExpanded(
    book: BookEntity,
    review: ReviewEntity?,
    onReviewClick: () -> Unit,
) {
    val coverUrl =
        if (book.cover == null && book.coverId != null) {
            "https://covers.openlibrary.org/b/id/${book.coverId}-M.jpg"
        } else {
            null
        }

    val reviewText =
        review?.reviewText
            ?.trimEnd()
            ?.takeIf { it.isNotBlank() }

    val isPlaceholder = reviewText == null

    val previewCharLimit = 380
    val reviewPreview =
        reviewText?.let {
            if (it.length > previewCharLimit) it.take(previewCharLimit) + "…" else it
        } ?: "No review given"


    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onReviewClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookCoverImage(
                    coverData = book.cover,
                    coverUrl = coverUrl,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (!book.authors.isNullOrBlank()) {
                        Text(
                            text = "By ${book.authors}",
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (review?.rating != null) {
                            StarsRow(
                                rating = review.rating,
                                starSize = 22.dp,
                                rowWidth = 130.dp,
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                        }

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
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }


                    review?.dateReviewed?.let { dateReviewed ->
                        val dateOnly = dateReviewed.split(" ").firstOrNull() ?: dateReviewed
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Finished: $dateOnly",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reviewPreview,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
                    ),
                maxLines = 12,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StarsRow(
    rating: Float,
    starSize: Dp,
    rowWidth: Dp,
) {
    Row(modifier = Modifier.width(rowWidth).height(starSize)) {
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
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.height(starSize),
                )
            }
        }
    }
}
