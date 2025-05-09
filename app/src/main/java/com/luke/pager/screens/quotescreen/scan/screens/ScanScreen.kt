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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.scan.ScanOutlineCanvas
import com.luke.pager.screens.quotescreen.scan.imageprocessing.processImageAndCluster
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.OutlineLevel
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.ScanPage
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val scannedPages by uiStateViewModel.scannedPages.collectAsState()

    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            try {
                val result = processImageAndCluster(context, capturedImageUri.toUri())
                textBlocks = result.textBlocks
                imageWidth = result.imageWidth
                imageHeight = result.imageHeight
                rotatedBitmap = result.rotatedBitmap
                allClusters = result.allClusters
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (capturedImageUri != null && imageWidth > 0 && imageHeight > 0) {
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val coroutineScope = rememberCoroutineScope()

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                navController.navigate("quotes") {
                                    popUpTo("quotes") { inclusive = true }
                                }
                                delay(500)
                                uiStateViewModel.clearScannedPages()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    val isLoading = rotatedBitmap == null

                    IconButton(
                        onClick = {
                            if (!isLoading) {
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
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Next / Confirm",
                            modifier = Modifier.size(24.dp),
                            tint = if (!isLoading) Color.Black else Color.Gray
                        )
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
                            pageIndex = 0
                        )

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
                                photoLauncher()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Page",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Add Page",
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { photoLauncher() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "Retake Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Retake",
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                if (scannedPages.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
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
            }
        } else {
            Text("No image yet", modifier = Modifier.fillMaxSize(), color = Color.White)
        }
    }
}
