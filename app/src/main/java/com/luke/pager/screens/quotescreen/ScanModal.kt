package com.luke.pager.screens.quotescreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.luke.pager.data.entities.BookEntity

@Composable
fun ScanModal(
    onDismiss: () -> Unit,
    overlayAlpha: Float,
    book: BookEntity
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val modalHeight = screenHeight / 1.5f
    val scrollState = rememberScrollState()

    CenteredModalScaffold(
        overlayAlpha = overlayAlpha,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 36.dp)
                .height(modalHeight)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .clickable(enabled = false) {}
                .verticalScroll(scrollState)
                .animateContentSize()
        ) {
            Text(
                text = "Scan quote from book: ${book.title}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // TODO: Add actual scan UI here (camera preview, OCR trigger, etc.)
            Text(
                text = "Camera preview goes here...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
