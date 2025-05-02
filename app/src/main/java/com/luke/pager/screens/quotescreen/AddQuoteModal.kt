package com.luke.pager.screens.quotescreen


import BookCoverImage
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.components.ScrollingTextField


@Composable
fun AddQuoteModal(
    onDismiss: () -> Unit,
    quoteViewModel: QuoteViewModel,
    overlayAlpha: Float,
    book: BookEntity,
    visible: Boolean,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val modalHeight = screenHeight / 1.5f
    val scrollState = rememberScrollState()

    var quoteText by remember(visible) { mutableStateOf("") }
    var pageNumber by remember(visible) { mutableStateOf("") }

    val containerHeightPx = with(density) { modalHeight.toPx().toInt() }

    BackHandler(enabled = visible) { onDismiss() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        CenteredModalScaffold(
            overlayAlpha = overlayAlpha,
            onDismiss = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 36.dp)
                    .height(modalHeight)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable(enabled = false) {}
                    .verticalScroll(scrollState)
                    .animateContentSize()
            ) {
                SubmitQuoteHeader(
                    onDismiss = onDismiss,
                    quoteText = quoteText,
                    pageNum = pageNumber,
                    bookId = book.id,
                    quoteViewModel = quoteViewModel,
                    scrollState = scrollState
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .heightIn(max = 150.dp),
                    ) {
                        BookCoverImage(
                            coverData = book.cover,
                            cornerRadius = 12,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 16.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        book.authors?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        book.firstPublishDate?.let {
                            Text(
                                text = book.firstPublishDate,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ScrollingTextField(
                        text = quoteText,
                        onTextChange = { quoteText = it },
                        scrollState = scrollState,
                        containerHeight = containerHeightPx,
                        existingSpaceTaken = 290,
                        insideText = "Quote..."
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = pageNumber,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            pageNumber = newValue
                        }
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .height(48.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    placeholder = { Text("__") },
                    leadingIcon = {
                        Text(
                            text = "Page:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

            }
        }
    }
}