
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun draggableTextSelection(
    fullText: String,
    modifier: Modifier = Modifier
): SelectionResult {
    var startCursorIndex by remember { mutableStateOf(0) }
    var endCursorIndex by remember { mutableStateOf(fullText.length) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var activeHandle by remember { mutableStateOf<Handle?>(null) }

    val scrollState = rememberScrollState()

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
            .fillMaxWidth()
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
            val startPos = layout.getCursorRect(startCursorIndex).topLeft
            val endPos = layout.getCursorRect(endCursorIndex).topLeft

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val touchOffset = layout.getOffsetForPosition(offset)
                                val distToStart = (touchOffset - startCursorIndex).absoluteValue
                                val distToEnd = (touchOffset - endCursorIndex).absoluteValue
                                activeHandle = if (distToStart < distToEnd) Handle.START else Handle.END
                            },
                            onDragEnd = {
                                activeHandle = null
                            },
                            onDrag = { change, _ ->
                                val offset = layout.getOffsetForPosition(change.position)
                                when (activeHandle) {
                                    Handle.START -> startCursorIndex = offset.coerceIn(0, fullText.length)
                                    Handle.END -> endCursorIndex = offset.coerceIn(0, fullText.length)
                                    null -> {}
                                }
                            }
                        )
                    }
            ) {
                drawCircle(
                    color = Color.Red,
                    radius = 12f,
                    center = Offset(startPos.x, startPos.y)
                )
                drawCircle(
                    color = Color.Blue,
                    radius = 12f,
                    center = Offset(endPos.x, endPos.y)
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
