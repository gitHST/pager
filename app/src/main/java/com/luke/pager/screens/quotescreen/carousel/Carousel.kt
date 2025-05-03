package com.luke.pager.screens.quotescreen.carousel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.luke.pager.screens.quotescreen.DisplayBook

@Composable
fun Carousel(
    books: List<DisplayBook>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    itemWidthPx: Float
) {
    if (books.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No books with covers", fontSize = 24.sp)
        }
    } else {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy((-30).dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 64.dp)
        ) {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

            itemsIndexed(
                books,
                key = { _, item -> item.book.id }
            ) { index, item ->
                val rawDistance = index - firstVisibleItemIndex
                val continuousDistance = rawDistance - (firstVisibleItemScrollOffset / itemWidthPx)
                Box(
                    modifier = Modifier
                        .height(160.dp)
                        .fillParentMaxHeight()
                        .zIndex(-continuousDistance),
                    contentAlignment = Alignment.Center
                ) {
                    CarouselItemContinuous(
                        imageBitmap = item.imageBitmap,
                        continuousDistance = continuousDistance,
                        isDummy = item.isDummy,
                        hasCover = item.hasCover // ✅ pass it through
                    )
                }
            }
        }
    }
}
