
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot

private const val HANDLE_TOUCH_RADIUS_DP = 24f

@Composable
fun draggableTextSelection(
    fullText: String,
    modifier: Modifier = Modifier
): SelectionResult {
    var startCursorIndex by remember { mutableIntStateOf(0) }
    var endCursorIndex by remember { mutableIntStateOf(fullText.length) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var activeHandle by remember { mutableStateOf<Handle?>(null) }

    // Scroll state for the entire selection area
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val handleTouchRadiusPx = with(density) { HANDLE_TOUCH_RADIUS_DP.dp.toPx() }

    val highlightedText = remember(startCursorIndex, endCursorIndex, fullText) {
        buildAnnotatedString {
            append(fullText)
            addStyle(
                style = SpanStyle(background = Color.Yellow),
                start = startCursorIndex.coerceAtMost(endCursorIndex),
                end = startCursorIndex.coerceAtLeast(endCursorIndex)
            )
        }
    }

    // The Box now scrolls everything (text + cursors)
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                awaitEachGesture(this.(fun PointerInputScope.() {
                    awaitPointerEventScope {
                        val down = awaitFirstDown()
                        val layout = textLayoutResult ?: return@awaitPointerEventScope
                        // Compute current handle positions
                        val startPos = layout.getCursorRect(startCursorIndex).center
                        val endPos = layout.getCursorRect(endCursorIndex).center
                        // Determine if touch is on one of the handles
                        val dxStart = down.position.x - startPos.x
                        val dyStart = down.position.y - startPos.y
                        val dxEnd = down.position.x - endPos.x
                        val dyEnd = down.position.y - endPos.y
                        activeHandle = when {
                            hypot(dxStart, dyStart) <= handleTouchRadiusPx -> Handle.START
                            hypot(dxEnd, dyEnd) <= handleTouchRadiusPx -> Handle.END
                            else -> null
                        }
                        // If not on a handle, do not consume—allow scroll
                        if (activeHandle == null) return@awaitPointerEventScope
                        // Handle drag for the active cursor
                        drag(down.id) { change ->
                            change.consumePositionChange()
                            val offsetIndex = layout.getOffsetForPosition(change.position)
                            if (activeHandle == Handle.START) {
                                startCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                            } else {
                                endCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                            }
                        }
                        activeHandle = null
                    }
                }))
            }
    ) {
        // Text + highlighting
        Text(
            text = highlightedText,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            onTextLayout = { result -> textLayoutResult = result }
        )

        // Draw cursors
        textLayoutResult?.let { layout ->
            val startPos = layout.getCursorRect(startCursorIndex).center
            val endPos = layout.getCursorRect(endCursorIndex).center
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.Red,
                    radius = 12f,
                    center = startPos
                )
                drawCircle(
                    color = Color.Blue,
                    radius = 12f,
                    center = endPos
                )
            }
        }
    }

    return SelectionResult(startCursorIndex, endCursorIndex)
}

private enum class Handle { START, END }

data class SelectionResult(
    val startIndex: Int,
    val endIndex: Int
)
