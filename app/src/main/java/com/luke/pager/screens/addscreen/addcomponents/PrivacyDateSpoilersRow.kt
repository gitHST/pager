package com.luke.pager.screens.addscreen.addcomponents

import Privacy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

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
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            PrivacyToggle(privacy, onLockToggle)
        }

        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            DatePickerDisplay(selectedDate, onDateClick)
        }

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