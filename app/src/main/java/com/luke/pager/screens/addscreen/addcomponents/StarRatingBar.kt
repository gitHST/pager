package com.luke.pager.screens.addscreen.addcomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        modifier =
        Modifier
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
            modifier =
            Modifier
                .fillMaxWidth(starRowWidthFraction)
                .height(starSize)
        ) {
            Row(modifier = Modifier.matchParentSize()) {
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
