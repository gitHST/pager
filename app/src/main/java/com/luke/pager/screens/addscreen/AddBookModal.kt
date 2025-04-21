package com.luke.pager.screens.addscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBook(book: OpenLibraryBook, onBack: () -> Unit, bookViewModel : BookViewModel, navController : NavHostController) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(0f) }
    var isPrivate by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    Box(modifier = Modifier.padding(8.dp)) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Go back",
                    modifier = Modifier.clickable(onClick = onBack),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Submit review",
                    modifier = Modifier.clickable
                    {
                        val now = LocalDate.now()

                        val finalDateTime = if (selectedDate != now) {
                            selectedDate.atStartOfDay()
                        } else {
                            selectedDate.atTime(LocalTime.now())
                        }

                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val dateReviewed = formatter.format(Date.from(finalDateTime.atZone(ZoneId.systemDefault()).toInstant()))

                        bookViewModel.submitReview(book, rating, reviewText, dateReviewed, isPrivate)
                        navController.navigate("diary") {
                            popUpTo("review_screen") { inclusive = true }
                            launchSingleTop = true
                        }

                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            BookRowUIClickable(book = book, onClick = {})
            Spacer(Modifier.height(16.dp))
            RatingBar(rating) { rating = it }
            Spacer(Modifier.height(12.dp))
            DatePickerPopup(
                showDialog = showDatePicker,
                datePickerState = datePickerState,
                onDismiss = { showDatePicker = false },
                onDateSelected = { selectedDate = it }
            )
            DateAndPrivateGrid(selectedDate, isPrivate, onDateClick = { showDatePicker = true }) {
                isPrivate = it
            }
            Spacer(Modifier.height(12.dp))
            ReviewTextField(reviewText) { reviewText = it }

            Spacer(Modifier.height(8.dp))

        }
    }
}

@Composable
private fun RatingBar(rating: Float, onRatingChange: (Float) -> Unit) {
    val starScale = 1.5f
    val starSize = 24.dp * starScale
    val starRowWidthFraction = 0.7f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(starSize)
    ) {
        IconButton(onClick = { if (rating > 0.0f) onRatingChange(rating - 0.5f) }) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Rating", tint = MaterialTheme.colorScheme.primary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(starRowWidthFraction)
                .height(starSize)
        ) {
            Row(modifier = Modifier.matchParentSize()) {
                for (i in 1..5) {
                    val icon = when {
                        rating >= i -> Icons.Filled.Star
                        rating == i - 0.5f -> Icons.AutoMirrored.Outlined.StarHalf
                        else -> Icons.Outlined.StarBorder
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                val target = i.toFloat()
                                onRatingChange(if (rating == target) target - 0.5f else target)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(starSize)
                        )
                    }
                }
            }
        }
        IconButton(onClick = { if (rating < 5f) onRatingChange(rating + 0.5f) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase Rating", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ReviewTextField(
    text: String,
    onTextChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val reviewHeight by animateDpAsState(
        targetValue = if (expanded) 320.dp else 180.dp,
        label = "ReviewHeight"
    )

    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(reviewHeight)
            .onFocusChanged { focusState: FocusState ->
                expanded = focusState.isFocused
            },
        placeholder = { Text("Review...") },
        shape = RoundedCornerShape(8.dp),
        singleLine = false,
        maxLines = 20
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerPopup(
    showDialog: Boolean,
    datePickerState: DatePickerState,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    if (!showDialog) return

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(date)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@Composable
private fun DateAndPrivateGrid(
    selectedDate: LocalDate,
    isLocked: Boolean,
    onDateClick: () -> Unit,
    onLockToggle: (Boolean) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    var labelState by remember { mutableStateOf(if (isLocked) "Private" else "Public") }
    var showLabel by remember { mutableStateOf(true) }

    LaunchedEffect(labelState) {
        showLabel = true
        delay(500)
        showLabel = false
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(75.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Read on...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedDate.format(formatter),
                modifier = Modifier
                    .clickable { onDateClick() }
                    .padding(6.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = {
                    onLockToggle(!isLocked)
                    labelState = if (!isLocked) "Private" else "Public"
                },
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isLocked) "Locked" else "Unlocked",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = showLabel) {
                Text(
                    text = labelState,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
