package com.luke.pager.screens.quotescreen.uicomponent

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
import androidx.compose.ui.unit.dp
import com.luke.pager.screens.quotescreen.ExtendedFabItem
import kotlinx.coroutines.delay

@Composable
fun FabOverlay(
    uiStateViewModel: QuoteUiStateViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var fullyCollapsed by remember { mutableStateOf(true) }
    var showActions by remember { mutableStateOf(false) }

    val showQuoteModal by uiStateViewModel.showQuoteModal.collectAsState()
    val showScanModal by uiStateViewModel.showScanModal.collectAsState()

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
                        uiStateViewModel.setShowScanModal(true)
                        isExpanded = false
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
}
