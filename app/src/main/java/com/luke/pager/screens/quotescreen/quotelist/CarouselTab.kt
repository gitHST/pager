package com.luke.pager.screens.quotescreen.quotelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.viewmodel.QuoteUiStateViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.components.HorizontalShadowDiv
import com.luke.pager.screens.quotescreen.addquote.AddQuoteModal
import com.luke.pager.screens.quotescreen.carousel.Carousel
import com.luke.pager.screens.quotescreen.carousel.byteArrayToImageBitmap
import com.luke.pager.screens.quotescreen.editquote.EditQuoteModal
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
            DummyBook(-1, "Dummy Book 1")
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

    val isExpanded by uiStateViewModel.isFabExpanded.collectAsState()
    val fullyCollapsed by uiStateViewModel.fullyCollapsed.collectAsState()
    val selectedBookId by uiStateViewModel.selectedBookId.collectAsState()
    val selectedBook = booksWithCovers.find { it.book.id == selectedBookId }

    val showQuoteModal by uiStateViewModel.showQuoteModal.collectAsState()
    val overlayAlpha by uiStateViewModel.overlayAlpha.collectAsState()
    val isSortAscendingState = uiStateViewModel.isSortAscending.collectAsState()
    val isSortAscending = isSortAscendingState.value

    val sortedQuotes = remember(quotes, isSortAscending) {
        if (isSortAscending) {
            quotes.sortedBy { it.dateAdded }
        } else {
            quotes.sortedByDescending { it.dateAdded }
        }
    }

    val listState = rememberLazyListState()
    val quotesListState = rememberLazyListState()
    val hasScrolledQuotes by remember {
        derivedStateOf {
            quotesListState.firstVisibleItemIndex > 0 || quotesListState.firstVisibleItemScrollOffset > 0
        }
    }
    val hasNotReachedEndOfQuotes by remember {
        derivedStateOf {
            val visibleItems = quotesListState.layoutInfo.visibleItemsInfo
            val totalItemsCount = quotesListState.layoutInfo.totalItemsCount
            if (visibleItems.isEmpty()) {
                false
            } else {
                val lastVisibleItem = visibleItems.last()
                lastVisibleItem.index < totalItemsCount - 1 ||
                    (
                        lastVisibleItem.index == totalItemsCount - 1 &&
                            lastVisibleItem.offset + lastVisibleItem.size > quotesListState.layoutInfo.viewportEndOffset
                        )
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val itemWidthPx = with(LocalDensity.current) { 120.dp.toPx() }

    val metaTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

    var activeQuoteId by remember { mutableStateOf<Long?>(null) }

    var editingQuote by remember { mutableStateOf<QuoteEntity?>(null) }

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
            val scale = screenHeightDp / 1000

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
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .height(30.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 36.dp, end = 36.dp, top = 16.dp, bottom = 16.dp)
            ) {
                if (sortedQuotes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No quotes for this book",
                            fontSize = 18.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .padding(top = 16.dp)
                        )

                        AnimatedVisibility(
                            visible = !isExpanded && fullyCollapsed && !showQuoteModal,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(100))
                        ) {
                            ExtendedFloatingActionButton(
                                text = { Text("Add", style = MaterialTheme.typography.labelLarge) },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Add quote") },
                                onClick = {
                                    uiStateViewModel.setFabExpanded(true)
                                    uiStateViewModel.setFullyCollapsed(false)
                                    uiStateViewModel.setShowFabActions(false)
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(width = 110.dp, height = 40.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 5.dp)
                    ) {
                        HorizontalShadowDiv(visible = hasScrolledQuotes)

                        LazyColumn(
                            state = quotesListState,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp)
                        ) {
                            items(sortedQuotes.size) { index ->
                                val quote = sortedQuotes[index]
                                val isActive = activeQuoteId == quote.id

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            activeQuoteId = if (isActive) null else quote.id
                                        }
                                ) {
                                    if (index != 0) {
                                        HorizontalDivider()
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = quote.quoteText,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        editingQuote = quote
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit quote",
                                                    tint = metaTextColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.weight(1f))

                                        if (quote.pageNumber != null) {
                                            Text(
                                                text = "p.${quote.pageNumber}",
                                                fontSize = 14.sp,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = metaTextColor
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalShadowDiv(
                            shadowFacingUp = true,
                            visible = hasNotReachedEndOfQuotes,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )

                        IconButton(
                            onClick = { uiStateViewModel.toggleSortOrder() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .offset(x = (47).dp)
                        ) {
                            Icon(
                                imageVector = if (isSortAscending) Icons.Default.South else Icons.Default.North,
                                contentDescription = "Toggle sort order",
                                tint = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { uiStateViewModel.setFabExpanded(true) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .offset(x = (47).dp, y = (47).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        selectedBook?.let { displayBook ->
            AddQuoteModal(
                onDismiss = { uiStateViewModel.setShowQuoteModal(false) },
                quoteViewModel = quoteViewModel,
                overlayAlpha = overlayAlpha,
                book = displayBook.book,
                visible = showQuoteModal,
                prefilledQuoteText = uiStateViewModel.prefilledQuoteText.collectAsState().value
            )

            editingQuote?.let { quoteToEdit ->
                EditQuoteModal(
                    onDismiss = { editingQuote = null },
                    quoteViewModel = quoteViewModel,
                    overlayAlpha = 0.5f,
                    book = displayBook.book,
                    quote = quoteToEdit,
                    visible = true
                )
            }
        }
    }
}
