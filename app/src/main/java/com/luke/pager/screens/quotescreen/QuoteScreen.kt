package com.luke.pager.screens.quotescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.navigation.compose.rememberNavController
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun QuotesScreen(bookViewModel: BookViewModel, quoteViewModel: QuoteViewModel) {
    val bookList by bookViewModel.books.collectAsState()
    val quotes by quoteViewModel.quotes.collectAsState()

    val placeholderBitmap = remember { createPlaceholderBitmap() }
    var showQuoteModal by remember { mutableStateOf(false) }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (showQuoteModal) 0.5f else 0f,
        animationSpec = tween(durationMillis = 400) // Try 500ms or slower like 700ms
    )


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

        Box(modifier = Modifier.weight(0.6f).padding(16.dp)) {
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

    var fabExpanded by remember { mutableStateOf(false) }
    var fabFullyCollapsed by remember { mutableStateOf(true) }
    var fabVisibleAfterDelay by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (fabExpanded) {
            // Tap anywhere to collapse
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(fabExpanded) {
                        awaitPointerEventScope {
                            while (fabExpanded) {
                                fabExpanded = false
                                fabFullyCollapsed = false
                                fabVisibleAfterDelay = false
                            }
                        }
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            // FABs with delay before showing
            AnimatedVisibility(
                visible = fabVisibleAfterDelay,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 200)
                ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 150)
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    ExtendedFabItem(
                        text = "Write",
                        icon = Icons.Default.FormatQuote,
                        onClick = { showQuoteModal = true }
                    )
                    ExtendedFabItem(
                        text = "Scan",
                        icon = Icons.Default.Create,
                        onClick = { /* Handle another action */ }
                    )
                }
            }

            // Fade in + FAB after collapse
            AnimatedVisibility(
                visible = !fabExpanded && fabFullyCollapsed,
                enter = fadeIn(animationSpec = tween(durationMillis = 150)),
                exit = fadeOut(animationSpec = tween(durationMillis = 100))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            fabExpanded = true
                            fabFullyCollapsed = false
                            fabVisibleAfterDelay = false
                        },
                        containerColor = FloatingActionButtonDefaults.containerColor
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Expand Actions")
                    }
                }
            }
        }

        // Delay before showing extended FABs
        LaunchedEffect(fabExpanded) {
            if (fabExpanded) {
                delay(50)
                fabVisibleAfterDelay = true
            } else {
                delay(200) // Wait for closing animation
                fabFullyCollapsed = true
            }
        }
    }
    if (showQuoteModal) {
        AddQuoteModal(
            onDismiss = { showQuoteModal = false },
            quoteViewModel = quoteViewModel,
            navController = rememberNavController(),
            overlayAlpha = overlayAlpha // NEW
        )
    }
}

@Composable
fun ExtendedFabItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        text = { Text(text) },
        icon = { Icon(icon, contentDescription = text) },
        onClick = onClick
    )
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