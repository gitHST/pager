package com.luke.pager.screens.quotescreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.graphics.createBitmap
import androidx.navigation.NavHostController
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.navigation.NavItem


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuotesScreen(
    bookViewModel: BookViewModel,
    quoteViewModel: QuoteViewModel,
    navController: NavHostController,
    currentRoute: String,
    navItems: List<NavItem>,
) {
    val bookList by bookViewModel.books.collectAsState()
    val quotes by quoteViewModel.quotes.collectAsState()
    val allQuotes by quoteViewModel.allQuotes.collectAsState()

    val placeholderBitmap = remember { createPlaceholderBitmap() }
    var showQuoteModal by remember { mutableStateOf(false) }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (showQuoteModal) 0.5f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabCount = 2

    val currentPageIndex = navItems.indexOfFirst { it.route == currentRoute }

    LaunchedEffect(Unit) {
        quoteViewModel.loadAllQuotes()
    }

    Column(
        modifier = Modifier.pointerInput(selectedTabIndex) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount > 50) {
                    if (selectedTabIndex > 0) {
                        selectedTabIndex--
                    } else {
                        val previousPage = (currentPageIndex - 1).coerceAtLeast(0)
                        navController.navigate(navItems[previousPage].route)
                    }
                } else if (dragAmount < -50) {
                    if (selectedTabIndex < tabCount - 1) {
                        selectedTabIndex++
                    } else {
                        val nextPage = (currentPageIndex + 1).coerceAtMost(navItems.lastIndex)
                        navController.navigate(navItems[nextPage].route)
                    }
                }
            }
        }
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Carousel", style = MaterialTheme.typography.bodyMedium) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("All", style = MaterialTheme.typography.bodyLarge) }
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
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "TabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> CarouselTab(
                        bookList = bookList,
                        quotes = quotes,
                        quoteViewModel = quoteViewModel,
                        placeholderBitmap = placeholderBitmap,
                        showQuoteModal = showQuoteModal,
                        setShowQuoteModal = { showQuoteModal = it },
                        overlayAlpha = overlayAlpha
                    )
                    1 -> AllQuotesTab(quotes = allQuotes, bookList = bookList)
                }
            }

            if (selectedTabIndex == 0) {
                FabOverlay(
                    showQuoteModal = showQuoteModal,
                    setShowQuoteModal = { showQuoteModal = it }
                )
            }
        }
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

data class DummyBook(val id: Long, val title: String) {
    fun toBookEntity(): BookEntity {
        return BookEntity(id = id, title = title, cover = null)
    }
}

fun createPlaceholderBitmap(): ImageBitmap {
    val width = 120
    val height = 180
    val bitmap = createBitmap(width, height)
    bitmap.eraseColor(android.graphics.Color.LTGRAY)
    return bitmap.asImageBitmap()
}