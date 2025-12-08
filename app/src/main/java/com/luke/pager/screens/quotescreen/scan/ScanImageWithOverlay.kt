package com.luke.pager.screens.quotescreen.scan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.OutlineLevel

@Composable
fun ScanImageWithOverlay(
    bitmap: android.graphics.Bitmap,
    allClusters: List<List<Text.TextBlock>>,
    imageWidth: Int,
    imageHeight: Int,
    outlineLevel: OutlineLevel,
    modifier: Modifier = Modifier,
    padding: Int = 16,
    toggledClusters: Set<Int> = emptySet(),
    globalClusterOrder: List<Pair<Int, Int>> = emptyList(),
    pageIndex: Int = 0,
    onClusterClick: ((Int) -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                .padding(padding.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Scanned page image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize(),
        )

        ScanOutlineCanvas(
            modifier = Modifier.matchParentSize(),
            pageIndex = pageIndex,
            allClusters = allClusters,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            outlineLevel = outlineLevel,
            toggledClusters = toggledClusters,
            globalClusterOrder = globalClusterOrder,
            onClusterClick = onClusterClick,
        )
    }
}
