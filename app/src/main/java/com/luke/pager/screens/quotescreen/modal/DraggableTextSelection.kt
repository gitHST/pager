
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.Handle
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.TextSelectionResult
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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val layout = textLayoutResult ?: return@awaitEachGesture

                    val startPos = layout.getCursorRect(startCursorIndex).center
                    val endPos = layout.getCursorRect(endCursorIndex).center

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

    return TextSelectionResult(startCursorIndex, endCursorIndex)
}
