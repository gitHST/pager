package com.luke.pager.screens.quotescreen

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.quotescreen.carousel.Carousel
import com.luke.pager.screens.quotescreen.carousel.byteArrayToImageBitmap
import kotlinx.coroutines.launch

@Composable
fun CarouselTab(
    bookList: List<BookEntity>,
    quotes: List<QuoteEntity>,
    quoteViewModel: QuoteViewModel,
    placeholderBitmap: ImageBitmap,
    showQuoteModal: Boolean,
    setShowQuoteModal: (Boolean) -> Unit,
    overlayAlpha: Float,
    showScanModal: Boolean,
    setShowScanModal: (Boolean) -> Unit
) {
    val booksWithConvertedCovers = remember(bookList) {
        val converted = bookList.mapNotNull { book ->
            book.cover?.let { coverBytes ->
                DisplayBook(
                    imageBitmap = byteArrayToImageBitmap(coverBytes),
                    book = book,
                    isDummy = false
                )
            }
        }.toMutableList()

        val dummyBooks = listOf(
            DummyBook(id = -1, title = "Dummy Book 1"),
            DummyBook(id = -2, title = "Dummy Book 2"),
            DummyBook(id = -3, title = "Dummy Book 3")
        )

        dummyBooks.forEach { dummy ->
            converted.add(
                DisplayBook(
                    imageBitmap = placeholderBitmap,
                    book = dummy.toBookEntity(),
                    isDummy = true
                )
            )
        }

        converted
    }

    var selectedBookId by remember { mutableStateOf<Long?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemWidthPx = with(LocalDensity.current) { 120.dp.toPx() }

    val selectedBook = booksWithConvertedCovers.find { it.book.id == selectedBookId }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val nearestItemIndex = listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > 150) 1 else 0
            coroutineScope.launch {
                listState.animateScrollToItem(nearestItemIndex)
            }

            val nearestBook = booksWithConvertedCovers.getOrNull(nearestItemIndex)
            nearestBook?.takeIf { !it.isDummy }?.let {
                selectedBookId = it.book.id
                quoteViewModel.loadQuotesForBook(it.book.id)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().weight(0.4f)) {
                Carousel(
                    books = booksWithConvertedCovers,
                    listState = listState,
                    itemWidthPx = itemWidthPx
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(40.dp)
            ) {
                if (quotes.isEmpty()) {
                    Text(
                        "No quotes for this book",
                        fontSize = 18.sp
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(quotes.size) { index ->
                            val quote = quotes[index]
                            Column {
                                Text(
                                    text = quote.quoteText,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    quote.pageNumber?.let { page ->
                                        Text(
                                            text = "p.$page",
                                            fontSize = 14.sp,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = 0.6f
                                                )
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
                        }
                    }
                }
            }
        }

        if (showQuoteModal && selectedBook != null) {
            AddQuoteModal(
                onDismiss = { setShowQuoteModal(false) },
                quoteViewModel = quoteViewModel,
                overlayAlpha = overlayAlpha,
                book = selectedBook.book
            )
        }
        if (showScanModal && selectedBook != null) {
            ScanModal(
                onDismiss = { setShowScanModal(false) },
                overlayAlpha = overlayAlpha,
                book = selectedBook.book
            )
        }

    }
}
