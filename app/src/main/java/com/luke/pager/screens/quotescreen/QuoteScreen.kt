package com.luke.pager.screens.quotescreen

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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import androidx.navigation.NavHostController
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.navigation.NavItem
import com.luke.pager.screens.components.NoBooksYetMessage
import com.luke.pager.screens.components.Title
import com.luke.pager.screens.quotescreen.tab.AllQuotesTab
import com.luke.pager.screens.quotescreen.tab.CarouselTab
import com.luke.pager.screens.quotescreen.uicomponent.FabOverlay
import com.luke.pager.screens.quotescreen.uicomponent.QuoteUiStateViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuotesScreen(
    bookViewModel: BookViewModel,
    quoteViewModel: QuoteViewModel,
    navController: NavHostController,
    currentRoute: String,
    navItems: List<NavItem>,
    snackbarHostState: SnackbarHostState,
    uiStateViewModel: QuoteUiStateViewModel
) {
    val bookList by bookViewModel.booksSortedByReviewDate.collectAsState()
    val quotes by quoteViewModel.quotes.collectAsState()
    val allQuotes by quoteViewModel.allQuotes.collectAsState()
    val placeholderBitmap = remember { createPlaceholderBitmap() }
    val selectedTabIndex by uiStateViewModel.selectedTabIndex.collectAsState()
    val currentPageIndex = navItems.indexOfFirst { it.route == currentRoute }

    LaunchedEffect(Unit) {
        quoteViewModel.loadAllQuotes()
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            delay(500)
            uiStateViewModel.setShowQuoteModal(false)
            uiStateViewModel.setShowScanModal(false)
        }
    }

    Column(
        modifier = Modifier.pointerInput(selectedTabIndex) {
            detectHorizontalDragGestures { _, dragAmount ->
                when {
                    dragAmount > 50 && selectedTabIndex > 0 -> uiStateViewModel.setSelectedTabIndex(selectedTabIndex - 1)
                    dragAmount < -50 && selectedTabIndex < 1 -> uiStateViewModel.setSelectedTabIndex(selectedTabIndex + 1)
                    dragAmount > 50 -> navController.navigate(navItems[(currentPageIndex - 1).coerceAtLeast(0)].route)
                    dragAmount < -50 -> navController.navigate(navItems[(currentPageIndex + 1).coerceAtMost(navItems.lastIndex)].route)
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
                containerColor = Color.Transparent,
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { uiStateViewModel.setSelectedTabIndex(0) },
                    icon = {
                        Box(
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                            modifier = Modifier
                                .indication(
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
                    onClick = {
                        uiStateViewModel.setSelectedTabIndex(1)
                    },
                    icon = {
                        Box(
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                            modifier = Modifier
                                .indication(
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
                                slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally { it } + fadeOut())
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

                        1 -> AllQuotesTab(quotes = allQuotes, bookList = bookList, uiStateViewModel = uiStateViewModel)
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
                            snackbarHostState = snackbarHostState
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
        text = { Text(text) },
        icon = { Icon(icon, contentDescription = text) },
        onClick = onClick,
        modifier = Modifier.width(110.dp)
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
