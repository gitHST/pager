package com.luke.pager.screens.quotescreen.modal

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import draggableTextSelection

@Composable
fun QuoteSelectionScreen(
    fullText: String,
    onCancel: () -> Unit,
    onDone: (selectedText: String) -> Unit
) {
    var startCursorIndex by remember { mutableIntStateOf(0) }
    var endCursorIndex by remember { mutableIntStateOf(fullText.length) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
                draggableTextSelection(
                    fullText = fullText,
                    modifier = Modifier.fillMaxSize()
                ).also { result ->
                    startCursorIndex = result.startIndex
                    endCursorIndex = result.endIndex
                }
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
                    val selectedText = fullText.substring(
                        startCursorIndex.coerceAtMost(endCursorIndex),
                        startCursorIndex.coerceAtLeast(endCursorIndex)
                    )
                    onDone(selectedText)
                }) {
                    Text("Done")
                }
            }
        }
    }
}
