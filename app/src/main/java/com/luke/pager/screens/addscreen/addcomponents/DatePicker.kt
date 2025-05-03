package com.luke.pager.screens.addscreen.addcomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            modifier =
            Modifier
                .clickable { onDateClick() }
                .padding(6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
