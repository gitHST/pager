
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.Handle
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.TextSelectionResult
import kotlin.math.hypot

private const val HANDLE_TOUCH_RADIUS_DP = 36f

@Composable
fun draggableTextSelection(
    fullText: String,
    modifier: Modifier = Modifier
): TextSelectionResult {
    var startCursorIndex by remember { mutableIntStateOf(0) }
    var endCursorIndex by remember { mutableIntStateOf(fullText.length) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var activeHandle by remember { mutableStateOf<Handle?>(null) }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val handleTouchRadiusPx = with(density) { HANDLE_TOUCH_RADIUS_DP.dp.toPx() }

    val handleColor = MaterialTheme.colorScheme.primary

    val highlightedText = remember(startCursorIndex, endCursorIndex, fullText) {
        buildAnnotatedString {
            append(fullText)
            addStyle(
                style = SpanStyle(background = Color(0xFFB3E5FC)),
                start = startCursorIndex.coerceAtMost(endCursorIndex),
                end = startCursorIndex.coerceAtLeast(endCursorIndex)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val layout = textLayoutResult ?: return@awaitEachGesture

                    fun handlePosFor(index: Int): Offset {
                        val rect = layout.getCursorRect(index.coerceIn(0, fullText.length))
                        val midY = (rect.top + rect.bottom) / 2f
                        return Offset(rect.left, midY)
                    }

                    val startPos = handlePosFor(startCursorIndex)
                    val endPos = handlePosFor(endCursorIndex)

                    val dxStart = down.position.x - startPos.x
                    val dyStart = down.position.y - startPos.y
                    val dxEnd = down.position.x - endPos.x
                    val dyEnd = down.position.y - endPos.y

                    activeHandle = when {
                        hypot(dxStart, dyStart) <= handleTouchRadiusPx -> Handle.START
                        hypot(dxEnd, dyEnd) <= handleTouchRadiusPx -> Handle.END
                        else -> null
                    }

                    if (activeHandle == null) return@awaitEachGesture

                    drag(down.id) { change ->
                        if (change.positionChange() != Offset.Zero) change.consume()
                        val offsetIndex = layout.getOffsetForPosition(change.position)
                        if (activeHandle == Handle.START) {
                            startCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                        } else {
                            endCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                        }
                    }

                    activeHandle = null
                }
            }
    ) {
        Text(
            text = highlightedText,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            onTextLayout = { result -> textLayoutResult = result }
        )

        textLayoutResult?.let { layout ->
            // Rects for the raw cursor positions
            val rectStart = layout.getCursorRect(startCursorIndex.coerceIn(0, fullText.length))
            val rectEnd = layout.getCursorRect(endCursorIndex.coerceIn(0, fullText.length))

            // Mid-Y for each rect
            val startMidY = (rectStart.top + rectStart.bottom) / 2f
            val endMidY = (rectEnd.top + rectEnd.bottom) / 2f

            // Anchors for each handle:
            //  - start anchored to its LEFT edge
            //  - end anchored to its RIGHT edge
            val startAnchor = Offset(rectStart.left, startMidY)
            val endAnchor = Offset(rectEnd.right, endMidY)

            // Leading in text order is purely based on indices, not geometry
            val startIsLeading = startCursorIndex <= endCursorIndex
            val endIsLeading = !startIsLeading

            Canvas(modifier = Modifier.matchParentSize()) {
                val r = 18f
                val nib = r

                fun drawHandle(anchor: Offset, isLeading: Boolean, isStartHandle: Boolean) {
                    // Leading handle: circle outside on the LEFT of its text edge
                    // Trailing handle: circle outside on the RIGHT of its text edge
                    val circleCenter = if (isLeading) {
                        // outside left
                        Offset(anchor.x - r, anchor.y)
                    } else {
                        // outside right
                        Offset(anchor.x + r, anchor.y)
                    }

                    // Draw main circle
                    drawCircle(
                        color = handleColor,
                        radius = r,
                        center = circleCenter
                    )

                    // Nib:
                    //  - Leading → nib to the RIGHT of circle (pointing into text)
                    //  - Trailing → nib to the LEFT of circle (pointing into text)
                    val nibTopLeft = if (isLeading) {
                        Offset(circleCenter.x, circleCenter.y)          // right side
                    } else {
                        Offset(circleCenter.x - nib, circleCenter.y)    // left side
                    }

                    drawRect(
                        color = handleColor,
                        topLeft = nibTopLeft,
                        size = Size(nib, nib)
                    )

                    // Subtle outline
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.15f),
                        radius = r,
                        center = circleCenter,
                        style = Stroke(width = 2f)
                    )
                }

                // Start handle at its own rect, role decided by index order
                drawHandle(
                    anchor = startAnchor,
                    isLeading = startIsLeading,
                    isStartHandle = true
                )

                // End handle at its own rect, opposite role
                drawHandle(
                    anchor = endAnchor,
                    isLeading = endIsLeading,
                    isStartHandle = false
                )
            }
        }
    }

    return TextSelectionResult(startCursorIndex, endCursorIndex)
}
