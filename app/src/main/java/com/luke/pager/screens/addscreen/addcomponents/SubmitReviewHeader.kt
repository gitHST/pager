package com.luke.pager.screens.addscreen.addcomponents

import Privacy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.screens.components.DualActionHeader
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
    modifier: Modifier = Modifier,
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    DualActionHeader(
        leftButtonText = " Go back ",
        onLeftClick = onBack,
        rightButtonText = " Submit review ",
        isRightButtonLoading = isSubmitting,
        onRightClick = {
            isSubmitting = true

            val now = LocalDate.now()
            val finalDateTime =
                if (selectedDate != now) {
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
        },
        scrollState = scrollState,
        modifier = modifier,
    )
}
