package com.luke.pager.screens.addscreen

import Privacy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.luke.pager.R
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var rating by remember { mutableFloatStateOf(0f) }
    var spoilers by remember { mutableStateOf(false) }
    var hasRated by remember { mutableStateOf(false) }
    var privacy by remember { mutableStateOf(Privacy.PUBLIC) }
    var reviewText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
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
            scrollState = scrollState, // pass scrollState
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
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
                    ReviewTextField(reviewText, { reviewText = it }, scrollState)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

}

@Composable
fun StarRatingBar(
    rating: Float,
    hasRated: Boolean,
    onRatingChange: (Float) -> Unit,
    onUserInteracted: () -> Unit,
    starScale: Float = 1.5f
) {
    val starSize = 24.dp * starScale
    val starRowWidthFraction = 0.7f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(starSize)
    ) {
        IconButton(onClick = {
            if (rating > 0.0f) {
                onRatingChange(rating - 0.5f)
                onUserInteracted()
            }
        }) {
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
                                onUserInteracted()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (hasRated) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(starSize)
                        )
                    }
                }
            }
        }
        IconButton(onClick = {
            if (rating < 5f) {
                onRatingChange(rating + 0.5f)
                onUserInteracted()
            }
        }) {
            Icon(Icons.Default.Add, contentDescription = "Increase Rating", tint = MaterialTheme.colorScheme.primary)
        }
    }
}


@Composable
private fun ReviewTextField(
    text: String,
    onTextChange: (String) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicTextField(
        value = text,
        onValueChange = {
            onTextChange(it)
            coroutineScope.launch {
                textLayoutResult?.let { layout ->
                    val lastLineBottom = layout.getLineBottom(layout.lineCount - 1)
                    scrollState.animateScrollTo(lastLineBottom.toInt())
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .heightIn(min = 180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        onTextLayout = { textLayoutResult = it },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default,
            keyboardType = KeyboardType.Text
        ),
        decorationBox = { innerTextField ->
            if (text.isEmpty()) {
                Text("Review...", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            innerTextField()
        }
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
private fun PrivacyDateSpoilersRow(
    selectedDate: LocalDate,
    privacy: Privacy,
    spoilers: Boolean,
    onDateClick: () -> Unit,
    onLockToggle: (Privacy) -> Unit,
    onSpoilerToggle: (Boolean) -> Unit
) {
    var currentSpoilerIconIndex by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fixed space for Privacy, center justified
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            PrivacyToggle(privacy, onLockToggle)
        }

        // Date in the center
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            DatePickerDisplay(selectedDate, onDateClick)
        }

        // Fixed space for Spoilers, center justified
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            SpoilerToggle(spoilers, currentSpoilerIconIndex) { newSpoilers, newIconIndex ->
                onSpoilerToggle(newSpoilers)
                currentSpoilerIconIndex = newIconIndex
            }
        }
    }
}


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
        animationSpec = tween(durationMillis = 300) // change duration here (300ms example)
    )


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
                .clickable {
                    val now = LocalDate.now()
                    val finalDateTime = if (selectedDate != now) {
                        selectedDate.atStartOfDay()
                    } else {
                        selectedDate.atTime(LocalTime.now())
                    }

                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val dateReviewed = formatter.format(Date.from(finalDateTime.atZone(ZoneId.systemDefault()).toInstant()))

                    val ratingToSubmit: Float? = if (hasRated) rating else null

                    bookViewModel.submitReview(book, ratingToSubmit, reviewText, dateReviewed, privacy, spoilers)

                    navController.navigate("diary") {
                        popUpTo("review_screen") { inclusive = true }
                        launchSingleTop = true
                    }
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(
                text = " Submit review ",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



@Composable
fun PrivacyToggle(
    privacy: Privacy,
    onLockToggle: (Privacy) -> Unit
) {
    val privacyIcons = listOf(Icons.Filled.Public, Icons.Filled.Lock, Icons.Filled.Group)
    val privacyLabels = listOf("Public", "Private", "Friends Only")
    val currentPrivacyIcon = privacyIcons[privacy.ordinal]
    val currentPrivacyLabel = privacyLabels[privacy.ordinal]

    var privacyShowLabel by remember { mutableStateOf(false) }
    var firstCompositionDone by remember { mutableStateOf(false) }

    LaunchedEffect(privacy) {
        if (firstCompositionDone) {
            privacyShowLabel = true
            delay(500)
            privacyShowLabel = false
        }
    }

    LaunchedEffect(Unit) {
        firstCompositionDone = true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            val nextOrdinal = (privacy.ordinal + 1) % Privacy.entries.size
            val newPrivacy = Privacy.entries[nextOrdinal]
            onLockToggle(newPrivacy)
        }) {
            Icon(
                imageVector = currentPrivacyIcon,
                contentDescription = currentPrivacyLabel,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(visible = privacyShowLabel) {
            Text(
                text = currentPrivacyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DatePickerDisplay(
    selectedDate: LocalDate,
    onDateClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "  Read on...",
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
}

@Composable
fun SpoilerToggle(
    spoilers: Boolean,
    currentSpoilerIconIndex: Int,
    onSpoilerToggle: (Boolean, Int) -> Unit
) {
    val spoilerIcons = listOf(
        R.drawable.ic_sentiment_very_dissatisfied,
        R.drawable.ic_sentiment_dissatisfied,
        R.drawable.ic_sentiment_extremely_dissatisfied,
        R.drawable.ic_sentiment_frustrated,
        R.drawable.ic_sentiment_sad,
        R.drawable.ic_sentiment_stressed,
        R.drawable.ic_sentiment_worried,
        R.drawable.ic_mood_bad
    )
    val currentSpoilerIconRes = spoilerIcons[currentSpoilerIconIndex]
    var spoilerLabelState by remember { mutableStateOf(if (spoilers) "Spoilers" else "No spoilers") }
    var spoilerShowLabel by remember { mutableStateOf(false) }
    var firstCompositionDone by remember { mutableStateOf(false) }

    LaunchedEffect(spoilerLabelState) {
        if (firstCompositionDone) {
            spoilerShowLabel = true
            delay(500)
            spoilerShowLabel = false
        }
    }

    LaunchedEffect(Unit) {
        firstCompositionDone = true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = {
                val newSpoilers = !spoilers
                val newIconIndex = (currentSpoilerIconIndex + 1) % spoilerIcons.size
                onSpoilerToggle(newSpoilers, newIconIndex)
                spoilerLabelState = if (newSpoilers) "Spoilers" else "No spoilers"
            }
        ) {
            if (spoilers) {
                Icon(
                    painter = painterResource(id = currentSpoilerIconRes),
                    contentDescription = "Spoilers On",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.SentimentSatisfiedAlt,
                    contentDescription = "Spoilers Off",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(visible = spoilerShowLabel) {
            Text(
                text = spoilerLabelState,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
