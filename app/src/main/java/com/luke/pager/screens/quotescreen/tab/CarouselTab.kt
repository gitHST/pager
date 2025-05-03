package com.luke.pager.screens.quotescreen.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.quotescreen.DisplayBook
import com.luke.pager.screens.quotescreen.DummyBook
import com.luke.pager.screens.quotescreen.carousel.Carousel
import com.luke.pager.screens.quotescreen.carousel.byteArrayToImageBitmap
import com.luke.pager.screens.quotescreen.modal.AddQuoteModal
import com.luke.pager.screens.quotescreen.modal.ScanModal
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel
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
        val realBooks = bookList.map { book ->
            val coverBitmap = book.cover?.let { byteArrayToImageBitmap(it) }
            DisplayBook(
                imageBitmap = coverBitmap ?: placeholderBitmap,
                book = book,
                isDummy = false,
                hasCover = coverBitmap != null
            )
        }.toMutableList()

        val dummies = listOf(
            DummyBook(-1, "Dummy Book 1"),
            DummyBook(-2, "Dummy Book 2"),
            DummyBook(-3, "Dummy Book 3"),
            DummyBook(-4, "Dummy Book 4"),
            DummyBook(-5, "Dummy Book 5"),
            DummyBook(-6, "Dummy Book 6"),
            DummyBook(-7, "Dummy Book 7"),
            DummyBook(-8, "Dummy Book 8"),
            DummyBook(-9, "Dummy Book 9"),
            DummyBook(-10, "Dummy Book 10")
        )

        dummies.forEach {
            realBooks.add(
                DisplayBook(
                    imageBitmap = placeholderBitmap,
                    book = it.toBookEntity(),
                    isDummy = true,
                    hasCover = false
                )
            )
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

    Box(Modifier.fillMaxSize().zIndex(2f)) {
        Column(Modifier.fillMaxSize()) {
            val density = LocalDensity.current.density
            val screenHeightDp = LocalWindowInfo.current.containerSize.height / density
            val scale = screenHeightDp / 800

            Box(Modifier.height((scale * 250).dp)) {
                Carousel(
                    books = booksWithCovers,
                    listState = listState,
                    itemWidthPx = itemWidthPx,
                    scale = scale
                )
            }

            Text(
                selectedBook?.book?.title ?: "",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .height(30.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 40.dp, end = 40.dp, top = 16.dp, bottom = 16.dp)
            ) {
                if (quotes.isEmpty()) {
                    Text(
                        "No quotes for this book",
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                } else {
                    HorizontalDivider()
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(quotes.size) { index ->
                                val quote = quotes[index]
                                Column {
                                    if (index != 0) {
                                        HorizontalDivider()
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = quote.quoteText,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontStyle = FontStyle.Italic
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (quote.pageNumber != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "p.${quote.pageNumber}",
                                                fontSize = 14.sp,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                                )
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                    }
                }
            }
        }

        selectedBook?.let { book ->
            AddQuoteModal(
                onDismiss = { uiStateViewModel.setShowQuoteModal(false) },
                quoteViewModel = quoteViewModel,
                overlayAlpha = overlayAlpha,
                book = book.book,
                visible = showQuoteModal
            )

            ScanModal(
                book = selectedBook.book,
                visible = showScanModal,
                overlayAlpha = overlayAlpha,
                onDismiss = { uiStateViewModel.setShowScanModal(false) }
            )
        }
    }
}
