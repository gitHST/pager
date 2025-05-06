package com.luke.pager.screens.quotescreen.scan.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.luke.pager.screens.quotescreen.scan.ScanPage

@Composable
fun MultiPagePreviewModal(
    scannedPages: List<ScanPage>,
    onDismiss: () -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            onDismiss()
        }
    }

    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (scannedPages.isNotEmpty()) {
            val current = scannedPages[currentPage]
            Image(
                bitmap = current.rotatedBitmap.asImageBitmap(),
                contentDescription = "Page ${currentPage + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(current.rotatedBitmap.width.toFloat() / current.rotatedBitmap.height.toFloat())
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentPage > 0) currentPage--
                    },
                    enabled = currentPage > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Page")
                }

                Text("${currentPage + 1} / ${scannedPages.size}")

                IconButton(
                    onClick = {
                        if (currentPage < scannedPages.lastIndex) currentPage++
                    },
                    enabled = currentPage < scannedPages.lastIndex
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Page")
                }
            }
        } else {
            Text("No pages to display")
        }
    }
}
