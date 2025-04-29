package com.luke.pager.screens.quotescreen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FabOverlay(
    showQuoteModal: Boolean,
    setShowQuoteModal: (Boolean) -> Unit
) {
    var fabExpanded by remember { mutableStateOf(false) }
    var fabFullyCollapsed by remember { mutableStateOf(true) }
    var fabVisibleAfterDelay by remember { mutableStateOf(false) }

    LaunchedEffect(showQuoteModal) {
        if (showQuoteModal) {
            fabExpanded = false
            fabFullyCollapsed = false
            fabVisibleAfterDelay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (fabExpanded && !showQuoteModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            fabExpanded = false
                            fabFullyCollapsed = false
                            fabVisibleAfterDelay = false
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
                visible = fabVisibleAfterDelay && !showQuoteModal,
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
                        icon = Icons.Default.FormatQuote,
                        onClick = {
                            setShowQuoteModal(true)
                            fabExpanded = false
                        }
                    )
                    ExtendedFabItem(
                        text = "Scan",
                        icon = Icons.Default.CameraAlt,
                        onClick = {
                            /* TODO: Handle Scan action */
                            fabExpanded = false
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = !fabExpanded && fabFullyCollapsed && !showQuoteModal,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)),
                exit = fadeOut(animationSpec = tween(durationMillis = 100))
            ) {
                Box(Modifier.padding(16.dp)) {
                    FloatingActionButton(
                        onClick = {
                            fabExpanded = true
                            fabFullyCollapsed = false
                            fabVisibleAfterDelay = false
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Expand Actions")
                    }
                }
            }
        }
    }

    LaunchedEffect(fabExpanded) {
        if (fabExpanded) {
            delay(50)
            fabVisibleAfterDelay = true
        } else {
            delay(200)
            fabFullyCollapsed = true
        }
    }
}