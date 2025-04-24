package com.luke.pager.screens.addscreen.addcomponents

import Privacy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.luke.pager.R
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PrivacyDateSpoilersRow(
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