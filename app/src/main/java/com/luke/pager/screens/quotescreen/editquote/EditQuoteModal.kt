package com.luke.pager.screens.quotescreen.editquote

import BookCoverImage
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.components.ScrollingTextField

@Composable
fun EditQuoteModal(
    onDismiss: () -> Unit,
    quoteViewModel: QuoteViewModel,
    overlayAlpha: Float = 0.5f,
    book: BookEntity,
    quote: QuoteEntity,
    visible: Boolean
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val modalHeight = screenHeight / 1.5f
    val containerHeightPx = with(density) { modalHeight.toPx().toInt() }

    val cleanedPrefilledText = quote.quoteText
        .replace("\n", " ")
        .replace("\r", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    var quoteText by remember(visible, cleanedPrefilledText) { mutableStateOf(cleanedPrefilledText) }
    var pageNumber by remember(visible) { mutableStateOf(quote.pageNumber?.toString() ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = visible) { onDismiss() }

    // 🔹 Build coverUrl from coverId if needed
    val coverUrl =
        if (book.cover == null && book.coverId != null) {
            "https://covers.openlibrary.org/b/id/${book.coverId}-M.jpg"
        } else {
            null
        }

    CenteredModalScaffold(
        onDismiss = onDismiss,
        overlayAlpha = overlayAlpha,
        visible = visible
    ) { scrollState ->

        EditQuoteHeader(
            onDismiss = onDismiss,
            quote = quote,
            quoteText = quoteText,
            pageNum = pageNumber,
            quoteViewModel = quoteViewModel,
            scrollState = scrollState
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .heightIn(max = 150.dp)
                ) {
                    BookCoverImage(
                        coverData = book.cover,
                        coverUrl = coverUrl
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    book.authors?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    book.firstPublishDate?.let {
                        Text(
                            text = book.firstPublishDate,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ScrollingTextField(
                    text = quoteText,
                    onTextChange = { quoteText = it },
                    scrollState = scrollState,
                    containerHeight = containerHeightPx,
                    existingSpaceTaken = 295,
                    insideText = "Quote..."
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = pageNumber,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            pageNumber = newValue
                        }
                    },
                    modifier = Modifier
                        .width(120.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    placeholder = { Text("_") },
                    leadingIcon = {
                        Text(
                            text = "Page:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showDeleteConfirm = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete quote",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirm = false
                            quoteViewModel.deleteQuote(quote)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Delete", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                title = {
                    Text("Delete quote", style = MaterialTheme.typography.titleLarge)
                },
                text = {
                    Text(
                        "Delete this quote?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    }
}
