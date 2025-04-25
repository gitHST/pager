package com.luke.pager.screens.addscreen

import Privacy
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.screens.addscreen.addcomponents.DatePickerPopup
import com.luke.pager.screens.addscreen.addcomponents.PrivacyDateSpoilersRow
import com.luke.pager.screens.addscreen.addcomponents.ReviewTextField
import com.luke.pager.screens.addscreen.addcomponents.StarRatingBar
import com.luke.pager.screens.addscreen.addcomponents.SubmitReviewHeader
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBook(
    book: OpenLibraryBook,
    onBack: () -> Unit,
    bookViewModel: BookViewModel,
    navController: NavHostController,
    containerHeight: Int
) {
    var rating by remember { mutableFloatStateOf(0f) }
    var spoilers by remember { mutableStateOf(false) }
    var hasRated by remember { mutableStateOf(false) }
    var privacy by remember { mutableStateOf(Privacy.PUBLIC) }
    var reviewText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

    Box(modifier = Modifier.fillMaxSize()) {
        val scrollState = rememberScrollState()
        SubmitReviewHeader(
            onBack = onBack,
            book = book,
            selectedDate = selectedDate,
            rating = rating,
            hasRated = hasRated,
            reviewText = reviewText,
            privacy = privacy,
            spoilers = spoilers,
            bookViewModel = bookViewModel,
            navController = navController,
            scrollState = scrollState,
            modifier =
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        )

        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .animateContentSize()
        ) {
            Spacer(Modifier.height(48.dp))
            BookRowUIClickable(book = book, onClick = {})
            Spacer(Modifier.height(16.dp))
            StarRatingBar(rating, hasRated, { rating = it }, { hasRated = true })
            Spacer(Modifier.height(12.dp))
            DatePickerPopup(
                showDialog = showDatePicker,
                datePickerState = datePickerState,
                onDismiss = { showDatePicker = false },
                onDateSelected = { selectedDate = it }
            )
            PrivacyDateSpoilersRow(
                selectedDate,
                privacy,
                spoilers,
                onDateClick = { showDatePicker = true },
                onLockToggle = { privacy = it },
                onSpoilerToggle = { spoilers = it }
            )
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ReviewTextField(reviewText, { reviewText = it }, scrollState, containerHeight)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
