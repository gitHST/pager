package com.luke.pager.screens.quotescreen.quotelist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteUiStateViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.components.NoBooksYetMessage
import com.luke.pager.screens.components.Title
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    bookViewModel: BookViewModel,
    quoteViewModel: QuoteViewModel,
    uiStateViewModel: QuoteUiStateViewModel,
    photoLauncher: () -> Unit
) {
    val bookList by bookViewModel.booksSortedByReviewDate.collectAsState()
    val quotes by quoteViewModel.quotes.collectAsState()
    val allQuotes by quoteViewModel.allQuotes.collectAsState()
    val placeholderBitmap = remember { createPlaceholderBitmap() }
    val selectedTabIndex by uiStateViewModel.selectedTabIndex.collectAsState()

    var isSwipeInProgress by remember { mutableStateOf(false) }
    var queuedDirection by remember { mutableIntStateOf(0) }
    val maxTabIndex = 1
    val swipeThreshold = 50f
    val swipeAnimationDurationMillis = 300

    fun performSwipe(direction: Int) {
        val targetIndex = (selectedTabIndex + direction).coerceIn(0, maxTabIndex)
        if (targetIndex != selectedTabIndex) {
            uiStateViewModel.setSelectedTabIndex(targetIndex)
        }
    }

    fun requestTabSwipe(direction: Int) {
        val targetIndex = (selectedTabIndex + direction).coerceIn(0, maxTabIndex)
        if (targetIndex == selectedTabIndex) {
            return
        }

        if (!isSwipeInProgress) {
            isSwipeInProgress = true
            queuedDirection = 0
            performSwipe(direction)
        } else {
            queuedDirection = direction
        }
    }

    LaunchedEffect(selectedTabIndex) {
        if (!isSwipeInProgress) return@LaunchedEffect

        delay(swipeAnimationDurationMillis.toLong())

        if (queuedDirection != 0) {
            val direction = queuedDirection
            queuedDirection = 0
            performSwipe(direction)
        } else {
            isSwipeInProgress = false
        }
    }

    LaunchedEffect(Unit) {
        quoteViewModel.loadAllQuotes()
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            uiStateViewModel.setShowQuoteModal(false)
        }
    }

    Column(
        modifier = Modifier.pointerInput(selectedTabIndex, isSwipeInProgress, queuedDirection) {
            detectHorizontalDragGestures { _, dragAmount ->
                when {
                    dragAmount > swipeThreshold && selectedTabIndex > 0 ->
                        requestTabSwipe(-1)

                    dragAmount < -swipeThreshold && selectedTabIndex < maxTabIndex ->
                        requestTabSwipe(1)
                }
            }
        }
    ) {
        Title("Quotes")
        if (bookList.isEmpty()) {
            NoBooksYetMessage()
        } else {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { uiStateViewModel.setSelectedTabIndex(0) },
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.indication(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewCarousel,
                                contentDescription = "Carousel"
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { uiStateViewModel.setSelectedTabIndex(1) },
                    icon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.indication(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "All"
                            )
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(
                                slideOutHorizontally { -it } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally { it } + fadeOut()
                            )
                        }
                    },
                    label = "TabContentAnimation"
                ) { index ->
                    when (index) {
                        0 -> CarouselTab(
                            bookList = bookList,
                            quotes = quotes,
                            quoteViewModel = quoteViewModel,
                            placeholderBitmap = placeholderBitmap,
                            uiStateViewModel = uiStateViewModel
                        )

                        1 -> AllQuotesTab(
                            quotes = allQuotes,
                            bookList = bookList,
                            uiStateViewModel = uiStateViewModel
                        )
                    }
                }

                if (selectedTabIndex == 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .zIndex(1f)
                    ) {
                        FabOverlay(
                            uiStateViewModel = uiStateViewModel,
                            photoLauncher = photoLauncher
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExtendedFabItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        text = { Text(text, style = MaterialTheme.typography.labelLarge) },
        icon = { Icon(icon, contentDescription = text) },
        onClick = onClick,
        modifier = Modifier.width(120.dp)
    )
}

data class DisplayBook(
    val imageBitmap: ImageBitmap,
    val book: BookEntity,
    val isDummy: Boolean,
    val hasCover: Boolean
)

data class DummyBook(val id: Long, val title: String) {
    fun toBookEntity(): BookEntity = BookEntity(id = id, title = title, cover = null)
}

fun createPlaceholderBitmap(): ImageBitmap {
    val width = 120
    val height = 180
    val bitmap = createBitmap(width, height)
    bitmap.eraseColor(android.graphics.Color.LTGRAY)
    return bitmap.asImageBitmap()
}
