package com.luke.pager.screens.quotescreen.modal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel


@Composable
fun ScanModal(
    uiStateViewModel: QuoteUiStateViewModel,
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
        val capturedImageUriState = uiStateViewModel.capturedImageUri.collectAsState()
        val capturedImageUri = capturedImageUriState.value

        if (capturedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(capturedImageUri),
                contentDescription = "Captured photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("No image yet")
        }

    }
}