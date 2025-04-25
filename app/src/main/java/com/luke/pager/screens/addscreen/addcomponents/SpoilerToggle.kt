package com.luke.pager.screens.addscreen.addcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.luke.pager.R
import kotlinx.coroutines.delay

@Composable
fun SpoilerToggle(
    spoilers: Boolean,
    currentSpoilerIconIndex: Int,
    onSpoilerToggle: (Boolean, Int) -> Unit
) {
    val spoilerIcons =
        listOf(
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
                val newIconIndex =
                    if (newSpoilers) {
                        (currentSpoilerIconIndex + 1) % spoilerIcons.size
                    } else {
                        currentSpoilerIconIndex
                    }
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
