package com.luke.pager.screens.quotescreen.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectionMagnifier(
    fullText: String,
    magnifierState: MagnifierState,
    modifier: Modifier = Modifier
) {
    val anchor = magnifierState.anchor
    val caretIndex = magnifierState.caretIndex

    if (!magnifierState.isActive || anchor == null || caretIndex == null) return

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val snippetRadiusChars = 20

    val baseStart = (caretIndex - snippetRadiusChars).coerceAtLeast(0)
    val baseEnd = (caretIndex + snippetRadiusChars).coerceAtMost(fullText.length)
    if (baseStart >= baseEnd) return

    val rawSnippet = fullText.substring(baseStart, baseEnd)
    val snippet = rawSnippet.replace('\n', ' ')

    if (snippet.isEmpty()) return

    val magnifierSizeDp = 96.dp
    val magnifierRadiusDp = magnifierSizeDp / 2
    val verticalOffsetPx = with(density) { -24.dp.toPx() }

    val magnifierCenterX = anchor.x
    val magnifierCenterY = anchor.y - verticalOffsetPx

    val offsetXdp = with(density) {
        magnifierCenterX.toDp() - magnifierRadiusDp
    }
    val offsetYdp = with(density) {
        magnifierCenterY.toDp() - magnifierRadiusDp
    }

    val magnifierTextStyle = TextStyle(
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    val markerStrokeWidthPx = with(density) { 2.dp.toPx() }

    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val handleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        Surface(
            modifier = Modifier
                .offset(x = offsetXdp, y = offsetYdp)
                .width(120.dp)
                .height(48.dp),
            shape = RoundedCornerShape(percent = 50),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val layoutResult = textMeasurer.measure(
                    text = AnnotatedString(snippet),
                    style = magnifierTextStyle,
                    maxLines = 1
                )

                val caretInSnippet = (caretIndex - baseStart).coerceIn(0, snippet.length)
                val caretRectInSnippet = layoutResult.getCursorRect(caretInSnippet)
                val caretX = caretRectInSnippet.center.x

                val centerX = size.width / 2f
                val centerY = size.height / 2f

                val textTopLeft = Offset(
                    x = centerX - caretX,
                    y = centerY - layoutResult.size.height / 2f
                )

                val isLeadingHandle = magnifierState.isLeadingHandle
                val highlightStartIndex: Int
                val highlightEndIndex: Int

                if (isLeadingHandle) {
                    highlightStartIndex = caretInSnippet
                    highlightEndIndex = snippet.length
                } else {
                    highlightStartIndex = 0
                    highlightEndIndex = caretInSnippet
                }

                val startRect = layoutResult.getCursorRect(
                    highlightStartIndex.coerceIn(0, snippet.length)
                )
                val endRect = layoutResult.getCursorRect(
                    highlightEndIndex.coerceIn(0, snippet.length)
                )

                val highlightLeftX = minOf(startRect.left, endRect.left)
                val highlightRightX = maxOf(startRect.left, endRect.left)

                val highlightLeft = textTopLeft.x + highlightLeftX
                val highlightRight = textTopLeft.x + highlightRightX
                val highlightTop = textTopLeft.y
                val highlightBottom = textTopLeft.y + layoutResult.size.height

                drawRect(
                    color = highlightColor,
                    topLeft = Offset(highlightLeft, highlightTop),
                    size = Size(
                        width = (highlightRight - highlightLeft),
                        height = (highlightBottom - highlightTop)
                    )
                )

                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = textTopLeft
                )

                val lineTop = Offset(centerX, centerY - layoutResult.size.height.toFloat())
                val lineBottom = Offset(centerX, centerY + layoutResult.size.height.toFloat())

                drawLine(
                    color = handleColor,
                    start = lineTop,
                    end = lineBottom,
                    strokeWidth = markerStrokeWidthPx
                )
            }
        }
    }
}
