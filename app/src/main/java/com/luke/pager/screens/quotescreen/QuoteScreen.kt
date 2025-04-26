package com.luke.pager.screens.quotescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.viewmodel.BookViewModel


@Composable
fun QuotesScreen(viewModel: BookViewModel) {
    val bookList by viewModel.books.collectAsState()

    val booksWithCovers = bookList.mapNotNull { book ->
        book.cover?.let { coverBytes ->
            byteArrayToImageBitmap(coverBytes) to book
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (booksWithCovers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No books with covers", fontSize = 24.sp)
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 64.dp)
            ) {
                items(booksWithCovers) { (imageBitmap, book) ->
                    CarouselItem(imageBitmap)
                }
            }
        }
    }
}
