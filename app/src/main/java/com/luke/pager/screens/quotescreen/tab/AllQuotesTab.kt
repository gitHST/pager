package com.luke.pager.screens.quotescreen.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel

@Composable
fun AllQuotesTab(
    quotes: List<QuoteEntity>,
    bookList: List<BookEntity>,
    uiStateViewModel: QuoteUiStateViewModel
) {
    val isSortAscendingState = uiStateViewModel.isSortAscending.collectAsState()
    val isSortAscending = isSortAscendingState.value

    val sortedQuotes = remember(quotes, isSortAscending) {
        if (isSortAscending) {
            quotes.sortedBy { it.dateAdded }
        } else {
            quotes.sortedByDescending { it.dateAdded }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 40.dp)
    ) {
        if (sortedQuotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No quotes yet",
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    sortedQuotes.forEachIndexed { index, quote ->
                        val associatedBook = bookList.find { it.id == quote.bookId }
                        val authors = associatedBook?.authors ?: "Unknown Author"
                        val year = associatedBook?.firstPublishDate?.take(4) ?: "Unknown Year"

                        if (index == 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Column {
                            Text(
                                text = quote.quoteText,
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "- $authors, $year",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                quote.pageNumber?.let { page ->
                                    Text(
                                        text = "p.$page",
                                        fontSize = 14.sp,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                            Box(
                                Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                            )
                        }

                        if (index == sortedQuotes.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                IconButton(
                    onClick = { uiStateViewModel.toggleSortOrder() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .offset(x = (45).dp, y = (8).dp)
                ) {
                    Icon(
                        imageVector = if (isSortAscending) Icons.Default.North else Icons.Default.South,
                        contentDescription = "Toggle sort order",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}
