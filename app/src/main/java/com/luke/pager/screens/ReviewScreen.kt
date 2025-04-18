package com.luke.pager.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.luke.pager.data.entities.ReviewEntity

@Composable
fun ReviewScreen(
    navController: NavController,
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
            Text("Review", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            review.dateReviewed?.let {
                Text("Reviewed on: $it", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review.reviewText ?: "You have not reviewed this book", fontSize = 16.sp)
        }
    } ?: run {
        Text(text = "Review not found", fontSize = 20.sp)
    }
}
