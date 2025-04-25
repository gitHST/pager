package com.luke.pager.screens.addscreen.addcomponents

import Privacy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
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
import kotlinx.coroutines.delay

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
