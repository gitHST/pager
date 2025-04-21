package com.luke.pager.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.ReviewEntity


@Composable
fun ReviewScreen(
    reviewId: Long,
    reviews: Map<Long, ReviewEntity?>
) {
    val review = reviews[reviewId]

    review?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Review",
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO: Handle menu click */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            review.dateReviewed?.let {
                val dateOnly = it.split(" ").firstOrNull() ?: it
                Text("Reviewed on: $dateOnly", fontSize = 14.sp)
            }
            review.rating?.let { ratingInt ->
                val rating = ratingInt.toFloat()
                val starScale = 1.5f
                val starSize = 24.dp * starScale
                val starRowWidthFraction = 0.7f

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(starRowWidthFraction)
                        .height(starSize)
                ) {
                    Row(modifier = Modifier.matchParentSize()) {
                        for (i in 1..5) {
                            val icon = when {
                                rating >= i -> Icons.Filled.Star
                                rating == i - 0.5f -> Icons.Outlined.StarHalf
                                else -> Icons.Outlined.StarBorder
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.height(starSize)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(review.reviewText ?: "You have not reviewed this book", fontSize = 16.sp)
        }
    } ?: run {
        Text(text = "Review not found", fontSize = 20.sp)
    }
}
