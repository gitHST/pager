package com.luke.pager.screens.addscreen

import BookCoverImage
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.network.SearchResult
import com.luke.pager.network.searchBooksSmart
import com.luke.pager.screens.components.Title
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SearchAndResultsModal(
    onDismiss: () -> Unit,
    bookViewModel: BookViewModel,
    navController: NavHostController,
    searchBooks: suspend (String) -> SearchResult = ::searchBooksSmart
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var books by remember { mutableStateOf<List<OpenLibraryBook>>(emptyList()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<OpenLibraryBook?>(null) }
    var containerHeight by remember { mutableIntStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasActivatedOnce by remember { mutableStateOf(false) }
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val imePadding = WindowInsets.ime.asPaddingValues()
    val imeVisible by remember {
        derivedStateOf { imePadding.calculateBottomPadding() > 0.dp }
    }
    val animatedTopPadding by animateDpAsState(36.dp)
    var forceCompressed by remember { mutableStateOf(false) }
    if (forceCompressed && imeVisible) {
        forceCompressed = false
    }
    val targetHeight =
        when {
            selectedBook != null -> screenHeight / 1.5f
            forceCompressed || imeVisible -> screenHeight / 2.2f
            else -> screenHeight / 1.5f
        }
    val animatedMaxHeight by animateDpAsState(targetHeight)

    LaunchedEffect(searchQuery) {
        searchJob?.cancel()

        if (searchQuery.isBlank()) {
            books = emptyList()
            isLoading = false
            return@LaunchedEffect
        }

        val currentQuery = searchQuery

        val deferredResult =
            async {
                searchBooks(currentQuery)
            }

        searchJob =
            launch {
                delay(500)
                if (currentQuery == searchQuery) {
                    isLoading = true
                    val result = deferredResult.await()
                    books = result.books
                    isLoading = false
                    active = true

                    result.errorMessage?.let {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                } else {
                    deferredResult.cancel()
                }
            }
    }

    Title("Review")
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.TopCenter)
                        .padding(top = animatedTopPadding)
                        .heightIn(max = animatedMaxHeight)
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 0.dp)
                        .onGloballyPositioned { coordinates ->
                            containerHeight = coordinates.size.height
                        }
                ) {
                    AnimatedContent(
                        targetState = selectedBook,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)).togetherWith(fadeOut(animationSpec = tween(200)))
                        }
                    ) { book ->
                        if (book != null) {
                            ReviewBook(
                                book = book,
                                onBack = { selectedBook = null },
                                bookViewModel = bookViewModel,
                                navController = navController,
                                containerHeight = containerHeight
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = {
                                    keyboardController?.hide()
                                },
                                active = active,
                                onActiveChange = { isActive ->
                                    active = isActive
                                    if (isActive && !hasActivatedOnce) {
                                        forceCompressed = true
                                        hasActivatedOnce = true
                                    }
                                },
                                placeholder = { Text("Search books...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-4).dp),
                                windowInsets = WindowInsets(0.dp),
                                colors =
                                SearchBarDefaults.colors(
                                    containerColor = Color.Transparent,
                                    dividerColor = Color.Transparent
                                )
                            ) {
                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    LazyColumn {
                                        if (books.isEmpty() && !isLoading && searchQuery.isNotBlank()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(24.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No results found",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        } else {
                                            items(books) { bookItem ->
                                                BookRowUIClickable(bookItem, onClick = { selectedBook = bookItem })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookRowUIClickable(
    book: OpenLibraryBook,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                BookCoverImage(
                    coverUrl = book.coverIndex?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.bodyMedium)
                book.authorName?.let {
                    Text(it.joinToString(), style = MaterialTheme.typography.bodySmall)
                }
                book.firstPublishYear?.let {
                    Text(
                        it.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        HorizontalDivider()
    }
}
