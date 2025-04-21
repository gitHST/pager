package com.luke.pager.screens.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.BookCover
import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.network.searchBooksSmart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SearchAndResultsModal(onDismiss: () -> Unit, bookViewModel: BookViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var books by remember { mutableStateOf<List<OpenLibraryBook>>(emptyList()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<OpenLibraryBook?>(null) }

    LaunchedEffect(searchQuery) {
        searchJob?.cancel()

        if (searchQuery.isBlank()) {
            books = emptyList()
            isLoading = false
            active = false
            return@LaunchedEffect
        }

        val currentQuery = searchQuery

        val deferredResult = async {
            searchBooksSmart(currentQuery)
        }

        searchJob = launch {
            delay(500)
            if (currentQuery == searchQuery) {
                isLoading = true
                books = deferredResult.await()
                isLoading = false
                active = true
            } else {
                deferredResult.cancel()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.9f)
                    .wrapContentHeight()
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 0.dp)
            ) {
                AnimatedContent(
                    targetState = selectedBook,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)).togetherWith(fadeOut(animationSpec = tween(200)))
                    }
                ) { book ->
                    if (book != null) {
                        ReviewBook(book = book, onBack = { selectedBook = null }, bookViewModel = bookViewModel)
                    } else {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {},
                            active = active,
                            onActiveChange = { active = it },
                            placeholder = { Text("Search books...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-4).dp),
                            windowInsets = WindowInsets(0.dp),
                            colors = SearchBarDefaults.colors(
                                containerColor = if (active) Color.Transparent else MaterialTheme.colorScheme.surface,
                                dividerColor = Color.Transparent
                            )
                        ) {
                            if (isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LazyColumn {
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

@Composable
fun BookRowUIClickable(book: OpenLibraryBook, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCover(book.cover_i)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.bodyLarge)
                book.author_name?.let {
                    Text(it.joinToString(), style = MaterialTheme.typography.bodySmall)
                }
                book.first_publish_year?.let {
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


