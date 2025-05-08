package com.luke.pager.screens.quotescreen.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuoteSelectionScreen(
    fullText: String,
    onCancel: () -> Unit,
    onDone: (selectedText: String) -> Unit
) {
    var startCursor by remember { mutableIntStateOf(0) }
    var endCursor by remember { mutableIntStateOf(fullText.length) }

    val displayedText = remember(startCursor, endCursor, fullText) {
        fullText.substring(
            startCursor.coerceAtLeast(0).coerceAtMost(fullText.length),
            endCursor.coerceAtLeast(0).coerceAtMost(fullText.length)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                androidx.compose.foundation.rememberScrollState().let { scrollState ->
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        Text(
                            text = fullText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // add visual indicators here for the selection range
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }

                Button(onClick = { onDone(displayedText) }) {
                    Text("Done")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start: $startCursor, End: $endCursor",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
