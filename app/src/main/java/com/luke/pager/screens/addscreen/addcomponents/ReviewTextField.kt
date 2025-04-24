package com.luke.pager.screens.addscreen.addcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ReviewTextField(
    text: String,
    onTextChange: (String) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
    containerHeight: Int
) {
    val coroutineScope = rememberCoroutineScope()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    val minHeightDp: Dp = with(density) { containerHeight.toDp() }

    BasicTextField(
        value = text,
        onValueChange = {
            onTextChange(it)
            coroutineScope.launch {
                textLayoutResult?.let { layout ->
                    val lastLineBottom = layout.getLineBottom(layout.lineCount - 1)
                    scrollState.animateScrollTo(lastLineBottom.toInt())
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeightDp - 362.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        onTextLayout = { textLayoutResult = it },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default,
            keyboardType = KeyboardType.Text
        ),
        decorationBox = { innerTextField ->
            if (text.isEmpty()) {
                Text("Review...", style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            innerTextField()
        }
    )
}