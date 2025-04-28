package com.luke.pager.screens.quotescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.viewmodel.QuoteViewModel


@Composable
fun AddQuoteModal(
    onDismiss: () -> Unit,
    quoteViewModel: QuoteViewModel,
    overlayAlpha: Float,
    book: BookEntity,
    bookCover: ImageBitmap
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val modalHeight = screenHeight / 1.5f
    val scrollState = rememberScrollState()

    var quoteText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )

        AnimatedVisibility(
            visible = overlayAlpha > 0f,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 36.dp)
                    .height(modalHeight)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable(enabled = false) {}
            ) {
                SubmitQuoteHeader(
                    onDismiss = onDismiss,
                    quoteText = quoteText,
                    bookId = book.id,
                    quoteViewModel = quoteViewModel,
                    scrollState = scrollState
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = bookCover,
                        contentDescription = "Book Cover",
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .padding(end = 16.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                    )

                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        book.authors?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    androidx.compose.material3.TextField(
                        value = quoteText,
                        onValueChange = { quoteText = it },
                        label = { Text("Enter your quote") },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    )
                }
            }
        }
    }
}
