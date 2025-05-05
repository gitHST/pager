package com.luke.pager.screens.quotescreen.modal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.quotescreen.imageprocessing.BlockBox
import com.luke.pager.screens.quotescreen.imageprocessing.ScanCanvas
import com.luke.pager.screens.quotescreen.imageprocessing.dbscan2D
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun ScanModal(
    uiStateViewModel: QuoteUiStateViewModel,
    visible: Boolean,
    overlayAlpha: Float,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = visible, onBack = onDismiss)
    CenteredModalScaffold(
        overlayAlpha = overlayAlpha,
        onDismiss = onDismiss,
        visible = visible
    ) {
        val capturedImageUriState = uiStateViewModel.capturedImageUri.collectAsState()
        val capturedImageUri = capturedImageUriState.value
        val context = LocalContext.current
        var textBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }
        var imageWidth by remember { mutableIntStateOf(1) }
        var imageHeight by remember { mutableIntStateOf(1) }
        var clusteredBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }

        LaunchedEffect(capturedImageUri) {
            if (capturedImageUri != null) {
                try {
                    val image = InputImage.fromFilePath(context, capturedImageUri.toUri())
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    val result = recognizer.process(image).await()
                    textBlocks = result.textBlocks
                    imageWidth = image.width
                    imageHeight = image.height


                    val allBoxes = mutableListOf<BlockBox>()
                    for (block in result.textBlocks) {
                        val boundingBox = block.boundingBox ?: continue
                        allBoxes.add(BlockBox(block, boundingBox))
                    }

                    val (clusters, noise) = dbscan2D(allBoxes, eps = 100f, minPts = 3)

                    val largestCluster = clusters.maxByOrNull { it.size } ?: emptyList()

                    clusteredBlocks = largestCluster.map { it.block }

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
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = capturedImageUri,
                        contentDescription = "Captured photo",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )

                    ScanCanvas(
                        modifier = Modifier.matchParentSize(),
                        textBlocks = textBlocks,
                        clusteredBlocks = clusteredBlocks,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight
                    )
                }
            } else {
                Text("Loading image...")
            }
        } else {
            Text("No image yet")
        }
    }
}
