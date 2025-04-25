package com.luke.pager.network

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

@Composable
fun BookCover(coverId: Int?, modifier: Modifier = Modifier) {
    val coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }

    val painter = rememberAsyncImagePainter(model = coverUrl)
    val painterState = painter.state

    var aspectRatio by remember { mutableFloatStateOf(2f / 3f) }

    if (painterState is AsyncImagePainter.State.Success) {
        val width = painterState.result.drawable.intrinsicWidth
        val height = painterState.result.drawable.intrinsicHeight
        if (width > 0 && height > 0) {
            aspectRatio = width.toFloat() / height.toFloat()
        }
    }

    val maxWidth = 80.dp
    val maxHeight = 120.dp

    Box(
        modifier = modifier
            .width(maxWidth)
            .height(maxHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .width(maxWidth)
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Image(
                painter = painter,
                contentDescription = "Book Cover",
                contentScale = ContentScale.Fit,
                modifier = Modifier.matchParentSize()
            )
        }

        when (painterState) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }

            is AsyncImagePainter.State.Error, is AsyncImagePainter.State.Empty -> {
                Text("No cover", style = MaterialTheme.typography.labelSmall)
            }

            else -> Unit
        }
    }
}