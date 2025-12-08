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
import com.luke.pager.screens.quotescreen.selection.MagnifierState
import kotlin.math.hypot

private const val HANDLE_TOUCH_RADIUS_DP = 36f

@Composable
fun draggableTextSelection(
    fullText: String,
    modifier: Modifier = Modifier,
    onMagnifierStateChange: (MagnifierState) -> Unit = {}
): TextSelectionResult {
    var startCursorIndex by remember { mutableIntStateOf(0) }
    var endCursorIndex by remember { mutableIntStateOf(fullText.length) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var activeHandle by remember { mutableStateOf<Handle?>(null) }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val handleTouchRadiusPx = with(density) { HANDLE_TOUCH_RADIUS_DP.dp.toPx() }

    val textColor = MaterialTheme.colorScheme.onBackground
    val selectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val handleColor = MaterialTheme.colorScheme.primary

    val highlightedText = remember(startCursorIndex, endCursorIndex, fullText, selectionColor) {
        buildAnnotatedString {
            append(fullText)
            addStyle(
                style = SpanStyle(background = selectionColor),
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

                    fun handlePosFor(index: Int, isStart: Boolean): Offset {
                        val rect = layout.getCursorRect(index.coerceIn(0, fullText.length))
                        val midY = (rect.top + rect.bottom) / 2f
                        return if (isStart) {
                            Offset(rect.left, midY)
                        } else {
                            Offset(rect.right, midY)
                        }
                    }

                    val startPos = handlePosFor(startCursorIndex, isStart = true)
                    val endPos = handlePosFor(endCursorIndex, isStart = false)

                    val dxStart = down.position.x - startPos.x
                    val dyStart = down.position.y - startPos.y
                    val dxEnd = down.position.x - endPos.x
                    val dyEnd = down.position.y - endPos.y

                    activeHandle = when {
                        hypot(dxStart, dyStart) <= handleTouchRadiusPx -> Handle.START
                        hypot(dxEnd, dyEnd) <= handleTouchRadiusPx -> Handle.END
                        else -> null
                    }

                    if (activeHandle == null) {
                        onMagnifierStateChange(MagnifierState(isActive = false))
                        return@awaitEachGesture
                    }

                    fun updateMagnifier() {
                        val startIsLeading = startCursorIndex <= endCursorIndex
                        val endIsLeading = !startIsLeading

                        val (rawAnchor, caretIndex, isLeading) = when (activeHandle) {
                            Handle.START -> {
                                Triple(
                                    handlePosFor(startCursorIndex, isStart = true),
                                    startCursorIndex,
                                    startIsLeading
                                )
                            }

                            Handle.END -> {
                                Triple(
                                    handlePosFor(endCursorIndex, isStart = false),
                                    endCursorIndex,
                                    endIsLeading
                                )
                            }

                            null -> Triple(null, null, true)
                        }

                        val adjustedAnchor = rawAnchor?.copy(
                            y = rawAnchor.y - scrollState.value.toFloat()
                        )

                        onMagnifierStateChange(
                            MagnifierState(
                                anchor = adjustedAnchor,
                                caretIndex = caretIndex,
                                isLeadingHandle = isLeading,
                                isActive = adjustedAnchor != null && caretIndex != null
                            )
                        )
                    }

                    updateMagnifier()

                    drag(down.id) { change ->
                        if (change.positionChange() != Offset.Zero) change.consume()
                        val offsetIndex = layout.getOffsetForPosition(change.position)
                        if (activeHandle == Handle.START) {
                            startCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                        } else {
                            endCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                        }
                        updateMagnifier()
                    }

                    activeHandle = null
                    onMagnifierStateChange(MagnifierState(isActive = false))
                }
            }
    ) {
        Text(
            text = highlightedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                color = textColor
            ),
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            onTextLayout = { result -> textLayoutResult = result }
        )

        textLayoutResult?.let { layout ->
            val rectStart = layout.getCursorRect(startCursorIndex.coerceIn(0, fullText.length))
            val rectEnd = layout.getCursorRect(endCursorIndex.coerceIn(0, fullText.length))

            val startMidY = (rectStart.top + rectStart.bottom) / 2f
            val endMidY = (rectEnd.top + rectEnd.bottom) / 2f

            val startAnchor = Offset(rectStart.left, startMidY)
            val endAnchor = Offset(rectEnd.right, endMidY)

            val startIsLeading = startCursorIndex <= endCursorIndex
            val endIsLeading = !startIsLeading

            Canvas(modifier = Modifier.matchParentSize()) {
                val r = 24f
                val nib = r

                fun drawHandle(anchor: Offset, isLeading: Boolean) {
                    val circleCenter = if (isLeading) {
                        Offset(anchor.x - r, anchor.y)
                    } else {
                        Offset(anchor.x + r, anchor.y)
                    }

                    drawCircle(
                        color = handleColor,
                        radius = r,
                        center = circleCenter
                    )

                    val nibTopLeft = if (isLeading) {
                        Offset(circleCenter.x, circleCenter.y)
                    } else {
                        Offset(circleCenter.x - nib, circleCenter.y)
                    }

                    drawRect(
                        color = handleColor,
                        topLeft = nibTopLeft,
                        size = Size(nib, nib)
                    )

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.15f),
                        radius = r,
                        center = circleCenter,
                        style = Stroke(width = 2f)
                    )
                }

                drawHandle(anchor = startAnchor, isLeading = startIsLeading)
                drawHandle(anchor = endAnchor, isLeading = endIsLeading)
            }
        }
    }

    return TextSelectionResult(startCursorIndex, endCursorIndex)
}
