package com.luke.pager.screens.quotescreen.modal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.imageprocessing.ScanCanvas
import com.luke.pager.screens.quotescreen.imageprocessing.processImageAndCluster
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel

@Composable
fun ScanScreen(
    uiStateViewModel: QuoteUiStateViewModel
) {
    val capturedImageUriState = uiStateViewModel.capturedImageUri.collectAsState()
    val capturedImageUri = capturedImageUriState.value
    val context = LocalContext.current

    var textBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }
    var imageWidth by remember { mutableIntStateOf(1) }
    var imageHeight by remember { mutableIntStateOf(1) }
    var clusteredBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }
    var allClusters by remember { mutableStateOf<List<List<Text.TextBlock>>>(emptyList()) }
    var rotatedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            try {
                val result = processImageAndCluster(context, capturedImageUri.toUri())
                textBlocks = result.textBlocks
                clusteredBlocks = result.clusteredBlocks
                imageWidth = result.imageWidth
                imageHeight = result.imageHeight
                rotatedBitmap = result.rotatedBitmap
                allClusters = result.allClusters
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (capturedImageUri != null) {
        if (imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (rotatedBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = rotatedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured photo",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                    )
                } else {
                    Text("Loading image...", modifier = Modifier.fillMaxSize())
                }

                ScanCanvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
                    allClusters = allClusters,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            }
        } else {
            Text("Loading image...", modifier = Modifier.fillMaxSize())
        }
    } else {
        Text("No image yet", modifier = Modifier.fillMaxSize())
    }
}