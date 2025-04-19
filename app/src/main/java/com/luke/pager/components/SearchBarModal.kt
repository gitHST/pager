package com.luke.pager.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luke.pager.network.apiresponse.OpenLibraryBook
import com.luke.pager.network.searchBooksSmart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBookModal(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var books by remember { mutableStateOf<List<OpenLibraryBook>>(emptyList()) }
    val scope = rememberCoroutineScope()

    var searchJob by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(false) }


    LaunchedEffect(active) {
        if (active && sheetState.currentValue == SheetValue.PartiallyExpanded) {
            sheetState.expand()
        }
    }

    LaunchedEffect(searchQuery) {
        searchJob?.cancel()

        if (searchQuery.isBlank()) {
            books = emptyList()
            isLoading = false
            return@LaunchedEffect
        }

        val currentQuery = searchQuery

        searchJob = scope.launch {
            delay(500)
            if (currentQuery == searchQuery) {
                isLoading = true
                books = searchBooksSmart(currentQuery)
                isLoading = false
            }
        }
    }




    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { active = false },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search books...") },
                modifier = Modifier.fillMaxWidth(),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    books.isNotEmpty() -> {
                        Column(modifier = Modifier.padding(8.dp)) {
                            books.forEach { book ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(book.title, style = MaterialTheme.typography.bodyLarge)
                                    book.author_name?.let {
                                        Text(it.joinToString(), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    searchQuery.isNotBlank() -> {
                        Text("No results", modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
