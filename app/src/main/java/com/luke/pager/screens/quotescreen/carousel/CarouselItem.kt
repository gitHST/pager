package com.luke.pager.screens.quotescreen.carousel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import kotlin.math.abs


@Composable
fun CarouselItemContinuous(
    imageBitmap: ImageBitmap,
    continuousDistance: Float,
    isDummy: Boolean,
    hasCover: Boolean,
    coverUrl: String? = null,
    cornerRadius: Int = 8,
    width: Dp = 110.dp,
    contentScale: ContentScale = ContentScale.Crop,
    scale: Float
) {
    val translateDensity = LocalDensity.current

    var offset = with(translateDensity) { 16.dp.toPx() }
    var animScale = 1.2f
    var rotationY = 0f
    var alpha = if (!isDummy) 1f else 0f
    var translationX = offset

    if (continuousDistance >= 0) {
        animScale = 1.2f - (0.2f * abs(continuousDistance).coerceAtMost(1f))
        rotationY = continuousDistance.coerceIn(-2f, 2f) * -20f
    } else {
        alpha = 1f + (1f * continuousDistance.coerceAtMost(1f))
        translationX = with(translateDensity) { (60 * continuousDistance).dp.toPx() } + offset
    }

    val animatedScale by animateFloatAsState(animScale)
    val animatedRotationY by animateFloatAsState(rotationY)
    val animatedAlpha by animateFloatAsState(alpha)
    val animatedTranslationX by animateFloatAsState(translationX)

    var aspectRatio by remember { mutableFloatStateOf(2f / 3f) }
    LaunchedEffect(imageBitmap) {
        if (imageBitmap.width > 0 && imageBitmap.height > 0) {
            aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()
        }
    }

    Box(
        modifier = Modifier
            .width(width * scale)
            .aspectRatio(aspectRatio)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                this.translationX = animatedTranslationX
                this.rotationY = animatedRotationY
                this.alpha = animatedAlpha
                cameraDistance = 8 * density
            }
            .zIndex(-continuousDistance)
            .clip(RoundedCornerShape(cornerRadius.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (hasCover) {
            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(cornerRadius.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cover",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
