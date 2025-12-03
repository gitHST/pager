package com.luke.pager.screens.quotescreen.quotelist

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.luke.pager.data.viewmodel.QuoteUiStateViewModel
import kotlinx.coroutines.delay

@Composable
fun FabOverlay(
    uiStateViewModel: QuoteUiStateViewModel,
    photoLauncher: () -> Unit
) {
    val isExpanded by uiStateViewModel.isFabExpanded.collectAsState()
    val showActions by uiStateViewModel.showFabActions.collectAsState()
    val showQuoteModal by uiStateViewModel.showQuoteModal.collectAsState()

    val context = LocalContext.current
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(50)
            uiStateViewModel.setShowFabActions(true)
        } else {
            delay(200)
            uiStateViewModel.setFullyCollapsed(true)
        }
    }

    LaunchedEffect(showQuoteModal) {
        if (!showQuoteModal) {
            uiStateViewModel.setFabExpanded(false)
            uiStateViewModel.setFullyCollapsed(true)
            uiStateViewModel.setShowFabActions(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (isExpanded && !showQuoteModal) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            uiStateViewModel.setFabExpanded(false)
                            uiStateViewModel.setShowFabActions(false)
                        }
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            AnimatedVisibility(
                visible = showActions && !showQuoteModal,
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    ExtendedFabItem(
                        text = "Write",
                        icon = Icons.Default.FormatQuote
                    ) {
                        uiStateViewModel.setShowQuoteModal(true)
                        uiStateViewModel.setFabExpanded(false)
                    }
                    ExtendedFabItem(
                        text = "Scan",
                        icon = Icons.Default.CameraAlt
                    ) {
                        uiStateViewModel.setFabExpanded(false)
                        uiStateViewModel.setShowFabActions(false)
                        photoLauncher()
                    }
                }
            }
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Camera permission is permanently denied. Please enable it in settings to use the Scan feature.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDeniedDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
