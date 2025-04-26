package com.luke.pager.screens.quotescreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun CarouselItem(imageBitmap: ImageBitmap, distanceFromFront: Int, scrollOffset: Float) {
    val clampedDistance = distanceFromFront.coerceIn(-2, 2)

    val targetScale = when (clampedDistance) {
        0 -> 1.2f
        1, -1 -> 1.0f
        else -> 0.8f
    }

    val targetRotationY = when {
        clampedDistance == 0 -> 0f
        clampedDistance > 0 -> -20f * clampedDistance
        else -> 20f * -clampedDistance
    }

    val baseAlpha = when (clampedDistance) {
        0 -> 1f
        1, -1 -> 0.8f
        else -> 0.5f
    }

    // Adjust front alpha based on scrollOffset
    val adjustedAlpha = if (clampedDistance == 0) {
        (1f - scrollOffset).coerceIn(0.3f, 1f)
    } else baseAlpha

    val animatedScale by animateFloatAsState(targetValue = targetScale)
    val animatedRotationY by animateFloatAsState(targetValue = targetRotationY)
    val animatedAlpha by animateFloatAsState(targetValue = adjustedAlpha)

    Box(
        modifier = Modifier
            .width(120.dp)
            .aspectRatio(2f / 3f)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                rotationY = animatedRotationY
                alpha = animatedAlpha
                cameraDistance = 8 * density
            }
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
