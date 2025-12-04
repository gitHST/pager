package com.luke.pager.screens.quotescreen.scan

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luke.pager.data.viewmodel.QuoteUiStateViewModel
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.OutlineLevel
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.ScanPage
import com.luke.pager.screens.quotescreen.selection.QuoteSelectionScreen

@Composable
fun MultiPagePreviewModal(
    scannedPages: List<ScanPage>,
    uiStateViewModel: QuoteUiStateViewModel,
    navController: NavController
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pageClickedOrder = remember(scannedPages) {
        scannedPages.map { mutableStateOf(listOf<Int>()) }.toMutableStateList()
    }

    val globalOrder = pageClickedOrder.flatMapIndexed { pageIndex, list ->
        list.value.map { clusterIndex -> pageIndex to clusterIndex }
    }

    var showConfirmationModal by remember { mutableStateOf(false) }
    var pendingCollectedText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (scannedPages.isNotEmpty()) {
            val current = scannedPages[currentPage]

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .aspectRatio(
                            current.rotatedBitmap.width.toFloat() /
                                    current.rotatedBitmap.height.toFloat()
                        )
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = current.rotatedBitmap.asImageBitmap(),
                        contentDescription = "Page ${currentPage + 1}",
                        modifier = Modifier.matchParentSize(),
                        alignment = Alignment.Center
                    )
                    ScanOutlineCanvas(
                        modifier = Modifier.matchParentSize(),
                        pageIndex = currentPage,
                        allClusters = current.allClusters,
                        imageWidth = current.imageWidth,
                        imageHeight = current.imageHeight,
                        outlineLevel = OutlineLevel.CLUSTER,
                        toggledClusters = pageClickedOrder[currentPage].value.toSet(),
                        globalClusterOrder = globalOrder,
                        onClusterClick = { clusterIndex ->
                            val currentList = pageClickedOrder[currentPage].value
                            pageClickedOrder[currentPage].value =
                                if (currentList.contains(clusterIndex)) {
                                    currentList - clusterIndex
                                } else {
                                    currentList + clusterIndex
                                }
                        }
                    )
                }

                Text(
                    text = "Tap the block(s) containing your quote in order – \nyou’ll trim it in the next step",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 72.dp)
                ) {
                    if (scannedPages.size > 1) {
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentPage > 0) currentPage-- },
                                enabled = currentPage > 0
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Page"
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("${currentPage + 1} / ${scannedPages.size}")

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { if (currentPage < scannedPages.lastIndex) currentPage++ },
                                enabled = currentPage < scannedPages.lastIndex
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Page"
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text("Back")
                }

                val anySelected = globalOrder.isNotEmpty()

                val doneAlpha by animateFloatAsState(
                    targetValue = if (anySelected) 1f else 0.4f,
                    label = "DoneButtonAlpha"
                )


                Button(
                    onClick = {
                        if (!anySelected) return@Button

                        val collectedText = globalOrder.joinToString("\n") { (pageIndex, clusterIndex) ->
                            val page = scannedPages[pageIndex]
                            val cluster = page.allClusters[clusterIndex]
                            cluster.joinToString("\n") { block -> block.text }
                        }

                        pendingCollectedText = collectedText
                        showConfirmationModal = true
                    },
                    enabled = anySelected,
                    modifier = Modifier.alpha(doneAlpha)
                ) {
                    Text("Done")
                }

            }
        } else {
            Text("No pages to display", modifier = Modifier.align(Alignment.Center))
        }
    }

    if (showConfirmationModal) {
        QuoteSelectionScreen(
            fullText = pendingCollectedText,
            onCancel = { showConfirmationModal = false },
            onDone = { selectedText ->
                uiStateViewModel.setPrefilledQuoteText(selectedText)
                uiStateViewModel.setShowQuoteModal(true)

                navController.navigate("quotes") {
                    popUpTo("multi_page_preview") { inclusive = true }
                }
            }
        )
    }
}
