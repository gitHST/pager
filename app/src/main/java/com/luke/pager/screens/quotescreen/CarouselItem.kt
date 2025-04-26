package com.luke.pager.screens.quotescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun CarouselItem(imageBitmap: ImageBitmap) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
