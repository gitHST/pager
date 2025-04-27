package com.luke.pager.screens.quotescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import kotlinx.coroutines.launch


@Composable
fun QuotesScreen(bookViewModel: BookViewModel, quoteViewModel: QuoteViewModel) {
    val bookList by bookViewModel.books.collectAsState()
    val quotes by quoteViewModel.quotes.collectAsState()

    val placeholderBitmap = remember { createPlaceholderBitmap() }

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

        // Add 3 dummy books
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

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val nearestItemIndex = listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > 150) 1 else 0
            coroutineScope.launch {
                listState.animateScrollToItem(nearestItemIndex)
            }

            // Load quotes for the nearest book (if not dummy)
            val nearestBook = booksWithConvertedCovers.getOrNull(nearestItemIndex)
            nearestBook?.takeIf { !it.isDummy }?.let {
                selectedBookId = it.book.id
                quoteViewModel.loadQuotesForBook(it.book.id)
            }
        }
    }
    Column() {
        Box(modifier = Modifier.fillMaxSize().weight(0.4f)) {
            if (booksWithConvertedCovers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No books with covers", fontSize = 24.sp)
                }
            } else {
                val firstVisibleItemIndex by remember {
                    derivedStateOf { listState.firstVisibleItemIndex }
                }
                val firstVisibleItemScrollOffset by remember {
                    derivedStateOf { listState.firstVisibleItemScrollOffset }
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy((-30).dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 64.dp)
                ) {
                    itemsIndexed(
                        booksWithConvertedCovers,
                        key = { _, item -> item.book.id }) { index, item ->
                        val rawDistance = index - firstVisibleItemIndex
                        val continuousDistance =
                            rawDistance - (firstVisibleItemScrollOffset / itemWidthPx)

                        CarouselItemContinuous(
                            imageBitmap = item.imageBitmap,
                            continuousDistance = continuousDistance,
                            isDummy = item.isDummy
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.weight(0.4f).padding(16.dp)) {
            val coroutineScope = rememberCoroutineScope()

            var selectedBookIndex by remember { mutableIntStateOf(0) }
            val selectableBooks = booksWithConvertedCovers.filter { !it.isDummy }

            var quoteText by remember { mutableStateOf(TextFieldValue("")) }
            var pageNumberText by remember { mutableStateOf(TextFieldValue("")) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectableBooks.isNotEmpty()) {
                    DropdownMenuBox(selectableBooks, selectedBookIndex) {
                        selectedBookIndex = it
                    }
                }

                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = { Text("Quote Text") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    val bookId = selectableBooks.getOrNull(selectedBookIndex)?.book?.id
                    if (!quoteText.text.isBlank() && bookId != null) {
                        val pageNum = pageNumberText.text.toIntOrNull()
                        coroutineScope.launch {
                            quoteViewModel.addQuote(
                                QuoteEntity(bookId = bookId, quoteText = quoteText.text, pageNumber = pageNum)
                            )
                            quoteViewModel.loadQuotesForBook(bookId)
                            quoteText = TextFieldValue("")
                            pageNumberText = TextFieldValue("")
                        }
                    }
                }) {
                    Text("Add Quote")
                }
            }
        }
        Box(modifier = Modifier.weight(0.2f).padding(16.dp)) {
            if (quotes.isEmpty()) {
                Text("No quotes for this book", fontSize = 18.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quotes.forEach { quote ->
                        Text("\"${quote.quoteText}\"", fontSize = 16.sp)
                        quote.pageNumber?.let {
                            Text("Page: $it", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

data class DisplayBook(
    val imageBitmap: ImageBitmap,
    val book: BookEntity,
    val isDummy: Boolean
)

// Dummy book helper
data class DummyBook(val id: Long, val title: String) {
    fun toBookEntity(): BookEntity {
        return BookEntity(id = id, title = title, cover = null)
    }
}

// Placeholder bitmap creator
fun createPlaceholderBitmap(): ImageBitmap {
    val width = 120
    val height = 180
    val bitmap = createBitmap(width, height)
    bitmap.eraseColor(android.graphics.Color.LTGRAY)
    return bitmap.asImageBitmap()
}

@Composable
fun DropdownMenuBox(books: List<DisplayBook>, selectedIndex: Int, onSelectedChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBookTitle = books.getOrNull(selectedIndex)?.book?.title ?: "Select a Book"

    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedBookTitle)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            books.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item.book.title) },
                    onClick = {
                        onSelectedChange(index)
                        expanded = false
                    }
                )
            }
        }
    }
}