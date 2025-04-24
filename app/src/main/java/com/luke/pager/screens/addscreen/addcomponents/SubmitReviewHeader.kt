package com.luke.pager.screens.addscreen.addcomponents

import Privacy
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun SubmitReviewHeader(
    onBack: () -> Unit,
    book: OpenLibraryBook,
    selectedDate: LocalDate,
    rating: Float,
    hasRated: Boolean,
    reviewText: String,
    privacy: Privacy,
    spoilers: Boolean,
    bookViewModel: BookViewModel,
    navController: NavHostController,
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    val targetBorderColor = if (scrollState.value > 0) Color.LightGray else Color.Transparent
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300)
    )
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    Row(
        modifier = modifier.zIndex(2f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, animatedBorderColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(
                text = " Go back ",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, animatedBorderColor, RoundedCornerShape(16.dp))
                .clickable(enabled = !isSubmitting) {
                    isSubmitting = true

                    val now = LocalDate.now()
                    val finalDateTime = if (selectedDate != now) {
                        selectedDate.atStartOfDay()
                    } else {
                        selectedDate.atTime(LocalTime.now())
                    }

                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val dateReviewed = formatter.format(Date.from(finalDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                    val ratingToSubmit: Float? = if (hasRated) rating else null

                    coroutineScope.launch {
                        bookViewModel.submitReview(book, ratingToSubmit, reviewText, dateReviewed, privacy, spoilers) {
                            navController.navigate("diary") {
                                popUpTo("review_screen") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            if (isSubmitting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(
                    text = " Submit review ",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

