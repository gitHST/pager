package com.luke.pager.screens.quotescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.BookViewModel
import kotlinx.coroutines.launch


@Composable
fun QuotesScreen(viewModel: BookViewModel) {
    val bookList by viewModel.books.collectAsState()

    val placeholderBitmap = remember { createPlaceholderBitmap() }

    // Data class for holding book info along with dummy flag
    data class DisplayBook(val imageBitmap: ImageBitmap, val book: BookEntity, val isDummy: Boolean)

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
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                itemsIndexed(booksWithConvertedCovers, key = { _, item -> item.book.id }) { index, item ->
                    val rawDistance = index - firstVisibleItemIndex
                    val continuousDistance = rawDistance - (firstVisibleItemScrollOffset / itemWidthPx)

                    CarouselItemContinuous(
                        imageBitmap = item.imageBitmap,
                        continuousDistance = continuousDistance,
                        isDummy = item.isDummy
                    )
                }
            }
        }
    }
}


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
