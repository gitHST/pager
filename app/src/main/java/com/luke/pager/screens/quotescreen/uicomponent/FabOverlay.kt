package com.luke.pager.screens.quotescreen.uicomponent

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.luke.pager.screens.quotescreen.ExtendedFabItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun FabOverlay(
    uiStateViewModel: QuoteUiStateViewModel,
    snackbarHostState: SnackbarHostState
) {
    var isExpanded by remember { mutableStateOf(false) }
    var fullyCollapsed by remember { mutableStateOf(true) }
    var showActions by remember { mutableStateOf(false) }

    val showQuoteModal by uiStateViewModel.showQuoteModal.collectAsState()
    val showScanModal by uiStateViewModel.showScanModal.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                uiStateViewModel.setShowScanModal(true)
            } else {
                val shouldShowRationale = when (context) {
                    is Activity -> ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.CAMERA
                    )
                    else -> false
                }

                if (!shouldShowRationale) {
                    showPermissionDeniedDialog = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Camera permission denied")
                    }
                }
            }
        }
    )


    LaunchedEffect(showQuoteModal || showScanModal) {
        if (showQuoteModal || showScanModal) {
            isExpanded = false
            fullyCollapsed = false
            showActions = false
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(50)
            showActions = true
        } else {
            delay(200)
            fullyCollapsed = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (isExpanded && !showQuoteModal && !showScanModal) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isExpanded = false
                            showActions = false
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
                visible = showActions && !showQuoteModal && !showScanModal,
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
                        isExpanded = false
                    }
                    ExtendedFabItem(
                        text = "Scan",
                        icon = Icons.Default.CameraAlt
                    ) {
                        isExpanded = false
                        showActions = false


                        val permissionCheck = ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            uiStateViewModel.setShowScanModal(true)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !isExpanded && fullyCollapsed && !showQuoteModal && !showScanModal,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(100))
            ) {
                Box(Modifier.padding(16.dp)) {
                    FloatingActionButton(onClick = {
                        isExpanded = true
                        fullyCollapsed = false
                        showActions = false
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Expand Actions")
                    }
                }
            }
        }
    }
    if (showPermissionDeniedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { androidx.compose.material3.Text("Permission Required") },
            text = { androidx.compose.material3.Text("Camera permission is permanently denied. Please enable it in settings to use the Scan feature.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showPermissionDeniedDialog = false
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                }) {
                    androidx.compose.material3.Text("Open Settings")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showPermissionDeniedDialog = false
                }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }

}
