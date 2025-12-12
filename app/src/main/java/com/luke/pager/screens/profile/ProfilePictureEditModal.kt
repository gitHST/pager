package com.luke.pager.screens.profile

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.luke.pager.screens.components.CenteredModalScaffold
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@Composable
fun ProfilePictureEditModal(
    visible: Boolean,
    imageUri: Uri?,
    initialZoom: Float,
    initialOffsetFraction: Offset,
    onDismiss: () -> Unit,
    onSave: (Uri?, Float, Offset, IntSize, Offset) -> Unit,
) {
    var zoom by remember(visible) { mutableFloatStateOf(initialZoom.coerceIn(1f, 3f)) }
    var offsetPx by remember(visible) { mutableStateOf(Offset.Zero) }
    var offsetFraction by remember(visible) { mutableStateOf(initialOffsetFraction) }
    var containerSize by remember(visible) { mutableStateOf(IntSize.Zero) }
    var initializedFromFraction by remember(visible) { mutableStateOf(false) }
    var interactionTick by remember(visible) { mutableIntStateOf(0) }

    var isInteracting by remember(visible) { mutableStateOf(false) }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cropOverlayAlpha",
    )

    var imageSize by remember(visible) { mutableStateOf(IntSize.Zero) }

    fun baseScale(): Float {
        val cw = containerSize.width.toFloat()
        val ch = containerSize.height.toFloat()
        val iw = imageSize.width.toFloat()
        val ih = imageSize.height.toFloat()

        if (cw <= 0f || ch <= 0f || iw <= 0f || ih <= 0f) return 1f
        return max(cw / iw, ch / ih)
    }

    fun clampOffset(
        raw: Offset,
        scale: Float,
    ): Offset {
        val cw = containerSize.width.toFloat()
        val ch = containerSize.height.toFloat()
        val iw = imageSize.width.toFloat()
        val ih = imageSize.height.toFloat()

        if (cw <= 0f || ch <= 0f || iw <= 0f || ih <= 0f) return raw

        val radius = min(cw, ch) / 2f

        val renderedW = iw * baseScale() * scale
        val renderedH = ih * baseScale() * scale

        val maxX = ((renderedW / 2f) - radius).coerceAtLeast(0f)
        val maxY = ((renderedH / 2f) - radius).coerceAtLeast(0f)

        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY),
        )
    }

    LaunchedEffect(containerSize, visible) {
        val width = containerSize.width.toFloat()
        val height = containerSize.height.toFloat()

        if (visible && !initializedFromFraction && width > 0f && height > 0f) {
            offsetPx =
                clampOffset(
                    Offset(
                        x = offsetFraction.x * width,
                        y = offsetFraction.y * height,
                    ),
                    zoom,
                )
            initializedFromFraction = true
        }
    }

    LaunchedEffect(zoom) {
        offsetPx = clampOffset(offsetPx, zoom)
    }

    LaunchedEffect(interactionTick) {
        isInteracting = true
        delay(200)
        isInteracting = false
    }

    CenteredModalScaffold(
        onDismiss = onDismiss,
        overlayAlpha = 0.5f,
        visible = visible,
    ) { _ ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
        ) {
            if (imageUri != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(0.95f)
                                .aspectRatio(1f)
                                .onSizeChanged { containerSize = it }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, gestureZoom, _ ->
                                        interactionTick++

                                        val newZoom = (zoom * gestureZoom).coerceIn(1f, 3f)
                                        val newOffset = offsetPx + pan

                                        zoom = newZoom
                                        offsetPx = clampOffset(newOffset, newZoom)
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = CircleShape,
                                    ),
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Profile picture preview",
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                        .graphicsLayer {
                                            val s = baseScale() * zoom
                                            scaleX = s
                                            scaleY = s
                                            translationX = offsetPx.x
                                            translationY = offsetPx.y
                                        },
                                contentScale = ContentScale.Fit,
                                onSuccess = { result ->
                                    val size = result.painter.intrinsicSize
                                    if (size.isSpecified) {
                                        imageSize = IntSize(size.width.toInt(), size.height.toInt())
                                    }
                                },
                            )

                            Canvas(
                                modifier = Modifier.matchParentSize(),
                            ) {
                                if (overlayAlpha > 0.01f) {
                                    val overlayColor =
                                        Color.Black.copy(alpha = 0.35f * overlayAlpha)
                                    val lineColor =
                                        Color.White.copy(alpha = 0.6f * overlayAlpha)
                                    val stroke = Stroke(width = 1.dp.toPx())

                                    drawRect(color = overlayColor)

                                    val w = size.width
                                    val h = size.height

                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = w / 3f, y = 0f),
                                        end = Offset(x = w / 3f, y = h),
                                        strokeWidth = stroke.width,
                                    )
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = 2f * w / 3f, y = 0f),
                                        end = Offset(x = 2f * w / 3f, y = h),
                                        strokeWidth = stroke.width,
                                    )

                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = 0f, y = h / 3f),
                                        end = Offset(x = w, y = h / 3f),
                                        strokeWidth = stroke.width,
                                    )
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = 0f, y = 2f * h / 3f),
                                        end = Offset(x = w, y = 2f * h / 3f),
                                        strokeWidth = stroke.width,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = zoom,
                    onValueChange = {
                        interactionTick++
                        zoom = it.coerceIn(1f, 3f)
                    },
                    onValueChangeFinished = {
                        interactionTick++
                    },
                    valueRange = 1f..3f,
                )
            } else {
                Text(
                    text = "No image selected.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val width = containerSize.width.toFloat().coerceAtLeast(1f)
                        val height = containerSize.height.toFloat().coerceAtLeast(1f)

                        val newFraction =
                            if (width > 0f && height > 0f) {
                                Offset(
                                    x = offsetPx.x / width,
                                    y = offsetPx.y / height,
                                )
                            } else {
                                offsetFraction
                            }

                        offsetFraction = newFraction
                        onSave(imageUri, zoom, newFraction, containerSize, offsetPx)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = imageUri != null,
                ) {
                    Text("Save")
                }
            }
        }
    }
}
