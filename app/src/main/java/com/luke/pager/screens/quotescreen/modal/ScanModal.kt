package com.luke.pager.screens.quotescreen.modal

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.screens.components.CenteredModalScaffold


@Composable
fun ScanModal(
    book: BookEntity,
    visible: Boolean,
    overlayAlpha: Float,
    onDismiss: () -> Unit,
) {
    BackHandler(enabled = visible, onBack = onDismiss)
    CenteredModalScaffold(
        overlayAlpha = overlayAlpha,
        onDismiss = onDismiss,
        visible = visible,
    ) {
        Text("Hi")
    }
}