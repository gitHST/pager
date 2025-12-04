package com.luke.pager.screens.quotescreen.editquote

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

@Composable
fun EditQuoteHeader(
    onDismiss: () -> Unit,
    quote: QuoteEntity,
    quoteText: String,
    pageNum: String,
    quoteViewModel: QuoteViewModel,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    DualActionHeader(
        leftButtonText = " Cancel ",
        onLeftClick = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        rightButtonText = " Done ",
        isRightButtonLoading = isSubmitting,
        onRightClick = {
            if (isSubmitting) return@DualActionHeader
            isSubmitting = true

            val updatedQuote = quote.copy(
                quoteText = quoteText,
                pageNumber = pageNum.toIntOrNull()
            )

            coroutineScope.launch {
                quoteViewModel.updateQuote(updatedQuote)
                isSubmitting = false
                onDismiss()
            }
        },
        scrollState = scrollState,
        modifier = modifier
    )
}
