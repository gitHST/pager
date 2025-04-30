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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    uiStateViewModel: QuoteUiStateViewModel
) {
    val booksWithCovers = remember(bookList) {
        val realBooks = bookList.mapNotNull { book ->
            book.cover?.let { cover ->
                DisplayBook(byteArrayToImageBitmap(cover), book, isDummy = false)
            }
        }.toMutableList()

        val dummies = listOf(
            DummyBook(-1, "Dummy Book 1"),
            DummyBook(-2, "Dummy Book 2"),
            DummyBook(-3, "Dummy Book 3")
        )

        dummies.forEach {
            realBooks.add(DisplayBook(placeholderBitmap, it.toBookEntity(), isDummy = true))
        }

        realBooks
    }

    val selectedBookId by uiStateViewModel.selectedBookId.collectAsState()
    val selectedBook = booksWithCovers.find { it.book.id == selectedBookId }

    val showQuoteModal by uiStateViewModel.showQuoteModal.collectAsState()
    val showScanModal by uiStateViewModel.showScanModal.collectAsState()
    val overlayAlpha by uiStateViewModel.overlayAlpha.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemWidthPx = with(LocalDensity.current) { 120.dp.toPx() }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val nearestIndex = listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > 150) 1 else 0

            coroutineScope.launch {
                listState.animateScrollToItem(nearestIndex)
            }

            booksWithCovers.getOrNull(nearestIndex)?.takeIf { !it.isDummy }?.let {
                uiStateViewModel.setSelectedBookId(it.book.id)
                quoteViewModel.loadQuotesForBook(it.book.id)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(0.4f)) {
                Carousel(
                    books = booksWithCovers,
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
                    Text("No quotes for this book", fontSize = 18.sp)
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
                                quote.pageNumber?.let { page ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.weight(1f))
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
                        }
                    }
                }
            }
        }

        selectedBook?.let { book ->
            if (showQuoteModal) {
                AddQuoteModal(
                    onDismiss = { uiStateViewModel.setShowQuoteModal(false) },
                    quoteViewModel = quoteViewModel,
                    overlayAlpha = overlayAlpha,
                    book = book.book
                )
            }

            if (showScanModal) {
                ScanModal(
                    book = selectedBook.book,
                    visible = showScanModal,
                    overlayAlpha = overlayAlpha,
                    onDismiss = { uiStateViewModel.setShowScanModal(false) }
                )
            }
        }
    }
}
