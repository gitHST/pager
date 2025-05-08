package com.luke.pager.screens.quotescreen.scan.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.scan.ScanOutlineCanvas
import com.luke.pager.screens.quotescreen.scan.imageprocessing.ClusterDistanceDebug
import com.luke.pager.screens.quotescreen.scan.imageprocessing.processImageAndCluster
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.OutlineLevel
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.ScanPage
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel

@Composable
fun ScanScreen(
    uiStateViewModel: QuoteUiStateViewModel,
    navController: NavController,
    photoLauncher: () -> Unit
) {
    val capturedImageUriState = uiStateViewModel.capturedImageUri.collectAsState()
    val capturedImageUri = capturedImageUriState.value
    val context = LocalContext.current

    var textBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }
    var imageWidth by remember { mutableIntStateOf(1) }
    var imageHeight by remember { mutableIntStateOf(1) }
    var allClusters by remember { mutableStateOf<List<List<Text.TextBlock>>>(emptyList()) }
    var rotatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var clusterDistances by remember { mutableStateOf<List<ClusterDistanceDebug>>(emptyList()) }

    var DEBUGScanSensitivity by rememberSaveable { mutableStateOf<Int?>(null) }
    var DEBUGshowDialog by rememberSaveable { mutableStateOf(true) }
    var DEBUGinputText by remember { mutableStateOf(TextFieldValue("")) }

    val scannedPages by uiStateViewModel.scannedPages.collectAsState()

    val ENABLE_DEBUG_DIALOG = false
    if (!ENABLE_DEBUG_DIALOG) {
        DEBUGScanSensitivity = 20
    }
    if (ENABLE_DEBUG_DIALOG && DEBUGshowDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        val enteredNumber = DEBUGinputText.text.toIntOrNull()
                        if (enteredNumber != null) {
                            DEBUGScanSensitivity = enteredNumber
                            DEBUGshowDialog = false
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("Cancel")
                }
            },
            title = { Text("Enter scan sensitivity") },
            text = {
                OutlinedTextField(
                    value = DEBUGinputText,
                    onValueChange = { DEBUGinputText = it },
                    label = { Text("Your number") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        )
    }

    LaunchedEffect(capturedImageUri, DEBUGScanSensitivity) {
        if (capturedImageUri != null && DEBUGScanSensitivity != null) {
            try {
                val result = processImageAndCluster(context, capturedImageUri.toUri(), eps = DEBUGScanSensitivity?.toFloat() ?: 20f)
                textBlocks = result.textBlocks
                imageWidth = result.imageWidth
                imageHeight = result.imageHeight
                rotatedBitmap = result.rotatedBitmap
                allClusters = result.allClusters
                clusterDistances = result.clusterDistances
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (capturedImageUri != null) {
        if (imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { photoLauncher() }) {
                        Text("Retake")
                    }
                    Button(onClick = {
                        if (rotatedBitmap != null) {
                            val newPage = ScanPage(
                                imageUri = capturedImageUri.toUri(),
                                rotatedBitmap = rotatedBitmap!!,
                                allClusters = allClusters,
                                imageWidth = imageWidth,
                                imageHeight = imageHeight
                            )
                            uiStateViewModel.addScannedPage(newPage)
                        }
                        photoLauncher()
                    }) {
                        Text("Add Page")
                    }
                    Button(
                        onClick = {
                            if (rotatedBitmap != null) {
                                val newPage = ScanPage(
                                    imageUri = capturedImageUri.toUri(),
                                    rotatedBitmap = rotatedBitmap!!,
                                    allClusters = allClusters,
                                    imageWidth = imageWidth,
                                    imageHeight = imageHeight
                                )
                                uiStateViewModel.addScannedPage(newPage)
                            }
                            navController.navigate("multi_page_preview")
                        }
                    ) {
                        Text("Done")
                    }
                }

                if (rotatedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                    ) {
                        Image(
                            bitmap = rotatedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured photo",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        ScanOutlineCanvas(
                            modifier = Modifier.matchParentSize(),
                            allClusters = allClusters,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            outlineLevel = OutlineLevel.CLUSTER,
                            toggledClusters = emptySet(),
                            globalClusterOrder = emptyList(),
                            pageClusterOrder = emptyList(),
                            pageIndex = 0,
                        )
                    }
                } else {
                    Text("Loading image...", modifier = Modifier.fillMaxWidth())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    if (scannedPages.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            items(scannedPages) { page ->
                                Image(
                                    bitmap = page.rotatedBitmap.asImageBitmap(),
                                    contentDescription = "Scanned page",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = clusterDistances,
                            key = { "${it.clusterA}-${it.clusterB}" }
                        ) { dist ->
                            Text(
                                text = "Cluster ${dist.clusterA} <-> ${dist.clusterB}: ${dist.distance}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        } else {
            Text("Loading image...", modifier = Modifier.fillMaxSize())
        }
    } else {
        Text("No image yet", modifier = Modifier.fillMaxSize())
    }
}
