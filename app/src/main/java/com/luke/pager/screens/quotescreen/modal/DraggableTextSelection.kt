
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = highlightedText,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                onTextLayout = { result -> textLayoutResult = result }
            )
        }

        textLayoutResult?.let { layout ->
            var startPos = layout.getCursorRect(startCursorIndex).center
            var endPos = layout.getCursorRect(endCursorIndex).center

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val distToStart = hypot(offset.x - startPos.x, offset.y - startPos.y)
                                val distToEnd = hypot(offset.x - endPos.x, offset.y - endPos.y)

                                activeHandle = when {
                                    distToStart <= handleTouchRadiusPx -> Handle.START
                                    distToEnd <= handleTouchRadiusPx -> Handle.END
                                    else -> null
                                }
                            },
                            onDragEnd = {
                                activeHandle = null
                            },
                            onDrag = { change, _ ->
                                val offsetIndex = layout.getOffsetForPosition(change.position)
                                when (activeHandle) {
                                    Handle.START -> startCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                                    Handle.END -> endCursorIndex = offsetIndex.coerceIn(0, fullText.length)
                                    null -> {}
                                }
                                startPos = layout.getCursorRect(startCursorIndex).center
                                endPos = layout.getCursorRect(endCursorIndex).center
                            }
                        )
                    }
            ) {
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
