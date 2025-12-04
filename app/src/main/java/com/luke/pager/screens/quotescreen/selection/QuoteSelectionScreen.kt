package com.luke.pager.screens.quotescreen.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import draggableTextSelection

@Composable
fun QuoteSelectionScreen(
    fullText: String,
    onCancel: () -> Unit,
    onDone: (selectedText: String) -> Unit
) {
    val cleanedFullText = remember(fullText) {
        fullText.replace(Regex("\\s*\\n+\\s*"), " ").trim()
    }

    var startCursorIndex by remember { mutableIntStateOf(0) }
    var endCursorIndex by remember { mutableIntStateOf(cleanedFullText.length) }

    var magnifierState by remember { mutableStateOf(MagnifierState()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Quote",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val selectionResult = draggableTextSelection(
                        fullText = cleanedFullText,
                        modifier = Modifier.fillMaxSize(),
                        onMagnifierStateChange = { state ->
                            magnifierState = state
                        }
                    )

                    startCursorIndex = selectionResult.startIndex
                    endCursorIndex = selectionResult.endIndex
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }

                    Button(onClick = {
                        val start = startCursorIndex.coerceAtMost(endCursorIndex)
                        val end = startCursorIndex.coerceAtLeast(endCursorIndex)

                        val selectedText = fullText.substring(
                            start.coerceIn(0, fullText.length),
                            end.coerceIn(0, fullText.length)
                        )
                        onDone(selectedText)
                    }) {
                        Text("Done")
                    }
                }
            }

            SelectionMagnifier(
                fullText = cleanedFullText,
                magnifierState = magnifierState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
