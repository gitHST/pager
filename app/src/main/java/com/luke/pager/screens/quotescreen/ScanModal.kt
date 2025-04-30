package com.luke.pager.screens.quotescreen

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.screens.components.CameraPermissionDeniedDialogue
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.components.PermissionResult
import com.luke.pager.screens.components.RequestCameraPermissionResult

@Composable
fun ScanModal(
    book: BookEntity,
    visible: Boolean,
    overlayAlpha: Float,
    onDismiss: () -> Unit,
) {
    var permission by remember { mutableStateOf<PermissionResult?>(null) }

    if (visible) {
        BackHandler(onBack = onDismiss)
    }

    CenteredModalScaffold(
        visible = visible,
        overlayAlpha = overlayAlpha,
        onDismiss = onDismiss
    ) {
        when (permission) {
            PermissionResult.Granted -> {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                val modalHeight = screenHeight / 1.5f
                val scroll = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 36.dp)
                        .height(modalHeight)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(scroll)
                        .animateContentSize()
                        .clickable(enabled = false) {}
                ) {
                    Text(
                        text = "Scan quote from book: ${book.title}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Camera preview goes here...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            PermissionResult.DeniedPermanently -> CameraPermissionDeniedDialogue()

            PermissionResult.DeniedTemporarily -> onDismiss()

            null -> RequestCameraPermissionResult { permission = it }
        }
    }
}
