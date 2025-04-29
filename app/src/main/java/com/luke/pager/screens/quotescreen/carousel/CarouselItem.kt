package com.luke.pager.screens.quotescreen.carousel

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.abs

@Composable
fun CarouselItemContinuous(imageBitmap: ImageBitmap, continuousDistance: Float, isDummy: Boolean) {

    val translateDensity = LocalDensity.current


    var scale = 1.2f
    var rotationY = 0f
    var alpha = if (!isDummy) 1f else 0f
    var translationX = 0f

    if (continuousDistance >= 0) {
        scale = 1.2f - (0.2f * abs(continuousDistance).coerceAtMost(1f))
        rotationY = continuousDistance.coerceIn(-2f, 2f) * -20f
    } else {
        alpha = 1f + (1f * continuousDistance.coerceAtMost(1f))
        translationX = with(translateDensity) { (60 * continuousDistance).dp.toPx() }
    }
    val animatedScale by animateFloatAsState(scale)
    val animatedRotationY by animateFloatAsState(rotationY)
    val animatedAlpha by animateFloatAsState(alpha)
    val animatedTranslationX by animateFloatAsState(translationX)

    Box(
        modifier = Modifier
            .width(110.dp)
            .aspectRatio(2f / 3f)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                this.translationX = animatedTranslationX
                this.rotationY = animatedRotationY
                this.alpha = animatedAlpha
                cameraDistance = 8 * density
            }
            .zIndex(-continuousDistance)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
