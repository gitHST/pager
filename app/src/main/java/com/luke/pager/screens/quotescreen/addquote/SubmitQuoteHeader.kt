package com.luke.pager.screens.quotescreen.addquote

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.components.DualActionHeader
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubmitQuoteHeader(
    onDismiss: () -> Unit,
    quoteText: String,
    pageNum: String,
    bookId: String,
    quoteViewModel: QuoteViewModel,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    DualActionHeader(
        leftButtonText = " Cancel ",
        onLeftClick = onDismiss,
        rightButtonText = " Submit quote ",
        isRightButtonLoading = isSubmitting,
        onRightClick = {
            val now = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateAdded = formatter.format(now)

            val quoteEntity =
                QuoteEntity(
                    id = "",
                    bookId = bookId,
                    quoteText = quoteText,
                    pageNumber = pageNum.toIntOrNull(),
                    dateAdded = dateAdded,
                )

            coroutineScope.launch {
                quoteViewModel.addQuote(quoteEntity)
                onDismiss()
            }
        },
        scrollState = scrollState,
        modifier = modifier,
    )
}
