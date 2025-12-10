package com.luke.pager.screens.profile

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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

    var isInteracting by remember(visible) { mutableStateOf(false) }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cropOverlayAlpha",
    )

    fun clampOffset(raw: Offset, scale: Float): Offset {
        val width = containerSize.width.toFloat()
        val height = containerSize.height.toFloat()

        if (width == 0f || height == 0f) return raw

        if (scale <= 1f) return Offset.Zero

        val radius = min(width, height) * 0.5f

        val halfWidthScaled = width * scale / 2f
        val halfHeightScaled = height * scale / 2f

        val minX = radius - halfWidthScaled
        val maxX = halfWidthScaled - radius

        val minY = radius - halfHeightScaled
        val maxY = halfHeightScaled - radius

        return Offset(
            x = raw.x.coerceIn(minX, maxX),
            y = raw.y.coerceIn(minY, maxY),
        )
    }

    LaunchedEffect(containerSize, visible) {
        val width = containerSize.width.toFloat()
        val height = containerSize.height.toFloat()

        if (visible && !initializedFromFraction && width > 0f && height > 0f) {
            offsetPx = clampOffset(
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
                                    detectDragGestures(
                                        onDragStart = {
                                            isInteracting = true
                                        },
                                        onDragEnd = {
                                            isInteracting = false
                                        },
                                        onDragCancel = {
                                            isInteracting = false
                                        },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val newOffset = offsetPx + dragAmount
                                        offsetPx = clampOffset(newOffset, zoom)
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
                                            scaleX = zoom
                                            scaleY = zoom
                                            translationX = offsetPx.x
                                            translationY = offsetPx.y
                                        },
                                contentScale = ContentScale.Crop,
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
                        isInteracting = true
                        zoom = it.coerceIn(1f, 3f)
                    },
                    onValueChangeFinished = {
                        isInteracting = false
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

                        val newFraction = if (width > 0f && height > 0f) {
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
