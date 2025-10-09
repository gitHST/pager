package com.luke.pager.screens.quotescreen.scan.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.scan.ScanImageWithOverlay
import com.luke.pager.screens.quotescreen.scan.imageprocessing.processImageAndCluster
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.OutlineLevel
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.ScanPage
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(
    uiStateViewModel: QuoteUiStateViewModel,
    navController: NavController,
    photoLauncher: () -> Unit
) {
    val context = LocalContext.current

    val scannedPages by uiStateViewModel.scannedPages.collectAsState()
    val capturedImageUri by uiStateViewModel.capturedImageUri.collectAsState()

    var textBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }
    var imageWidth by remember { mutableIntStateOf(1) }
    var imageHeight by remember { mutableIntStateOf(1) }
    var allClusters by remember { mutableStateOf<List<List<Text.TextBlock>>>(emptyList()) }
    var rotatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isLaunchingCamera by remember { mutableStateOf(false) }
    var targetPageCount by remember { mutableIntStateOf(1) }
    var isRetaking by remember { mutableStateOf(false) }
    var selectedPage by remember { mutableStateOf<ScanPage?>(scannedPages.lastOrNull()) }

    LaunchedEffect(scannedPages.size) {
        if (targetPageCount < scannedPages.size) {
            targetPageCount = scannedPages.size
        }
    }

    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            Log.d("ScanScreen", "Captured image URI: $capturedImageUri | isRetaking=$isRetaking")
            isLaunchingCamera = true
            try {
                val result = processImageAndCluster(context, capturedImageUri!!.toUri())
                textBlocks = result.textBlocks
                imageWidth = result.imageWidth
                imageHeight = result.imageHeight
                rotatedBitmap = result.rotatedBitmap
                allClusters = result.allClusters

                rotatedBitmap?.let {
                    val newPage = ScanPage(
                        imageUri = capturedImageUri!!.toUri(),
                        rotatedBitmap = it,
                        allClusters = allClusters,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight
                    )

                    val updatedPages = scannedPages.toMutableList()

                    if (isRetaking) {
                        val indexToReplace = scannedPages.indexOf(selectedPage)
                        Log.d("ScanScreen", "Retaking page at index: $indexToReplace | selectedPage=$selectedPage")
                        if (indexToReplace >= 0) {
                            updatedPages[indexToReplace] = newPage
                            uiStateViewModel.setScannedPages(updatedPages)
                            Log.d("ScanScreen", "Updated scannedPages after retake: ${updatedPages.map { it.imageUri }}")
                        } else {
                            Log.d("ScanScreen", "WARNING: selectedPage not found in scannedPages")
                        }
                    } else if (scannedPages.size < targetPageCount) {
                        updatedPages.add(newPage)
                        uiStateViewModel.setScannedPages(updatedPages)
                        targetPageCount = updatedPages.size
                        Log.d("ScanScreen", "Added new page, scannedPages size=${updatedPages.size}")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ScanScreen", "Error processing image: ${e.message}")
            }
        }
    }


    LaunchedEffect(scannedPages) {
        snapshotFlow { scannedPages.map { it.imageUri } }
            .distinctUntilChanged()
            .collectLatest { _ ->
                Log.d("ScanScreen", "snapshotFlow triggered | scannedPages=${scannedPages.map { it.imageUri }} | isRetaking=$isRetaking | targetPageCount=$targetPageCount")

                if (isRetaking) {
                    selectedPage = scannedPages.firstOrNull { it.rotatedBitmap == rotatedBitmap }
                    Log.d("ScanScreen", "Retake selectedPage set to: $selectedPage")
                    isRetaking = false
                } else if (scannedPages.size == targetPageCount) {
                    selectedPage = scannedPages.lastOrNull()
                    Log.d("ScanScreen", "Add page selectedPage set to last: $selectedPage")
                } else {
                    Log.d("ScanScreen", "No selection change")
                }
                isLaunchingCamera = false
            }

    }


    Box(modifier = Modifier.fillMaxSize()) {
        if (scannedPages.isNotEmpty() && imageWidth > 0 && imageHeight > 0) {
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
                                uiStateViewModel.clearScannedPages()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedPage?.let {
                                navController.navigate("multi_page_preview")
                            }
                        },
                        enabled = !isLaunchingCamera,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Next / Confirm",
                            modifier = Modifier.size(24.dp),
                            tint = if (!isLaunchingCamera) Color.Black else Color.Gray
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.75f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isLaunchingCamera && selectedPage?.rotatedBitmap != null) {
                            Box(modifier = Modifier.matchParentSize()) {
                                ScanImageWithOverlay(
                                    bitmap = selectedPage!!.rotatedBitmap,
                                    allClusters = selectedPage!!.allClusters,
                                    imageWidth = selectedPage!!.imageWidth,
                                    imageHeight = selectedPage!!.imageHeight,
                                    outlineLevel = OutlineLevel.CLUSTER,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Button(
                                    onClick = {
                                        isRetaking = true
                                        photoLauncher()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    elevation = ButtonDefaults.buttonElevation(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Autorenew,
                                            contentDescription = "Retake Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text("Retake", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }

                                Button(
                                    onClick = {
                                        targetPageCount++
                                        photoLauncher()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    elevation = ButtonDefaults.buttonElevation(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Page",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text("Add Page", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        } else {
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
                                .weight(0.25f)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(scannedPages, key = { it.imageUri.toString() }) { page ->
                                Image(
                                    bitmap = page.rotatedBitmap.asImageBitmap(),
                                    contentDescription = "Scanned page",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp)
                                        .clickable {
                                            selectedPage = page
                                            Log.d("ScanScreen", "Thumbnail clicked, selectedPage=$selectedPage")
                                        }
                                        .then(
                                            if (selectedPage == page)
                                                Modifier.border(3.dp, MaterialTheme.colorScheme.primary)
                                            else Modifier
                                        )
                                )

                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
