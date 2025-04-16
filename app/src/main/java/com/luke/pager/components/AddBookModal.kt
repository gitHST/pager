package com.luke.pager.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookModal(
    books: List<BookEntity>,
    sheetState: SheetState,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel
) {
    var title by remember { mutableStateOf("") }
    var authors by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Enter Book Details", fontSize = 20.sp)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = authors,
                onValueChange = { authors = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Review (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6,
                singleLine = false
            )

            Button(
                onClick = {
                    val actualTitle = generateUniqueTitle(title, books)
                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    scope.launch {
                        // 1. Insert Book
                        val book = BookEntity(
                            title = actualTitle,
                            authors = authors.ifBlank { "Unknown Author" },
                            dateAdded = currentDate
                        )
                        val bookId = bookViewModel.insertAndReturnId(book)

                        // 2. Insert Review with linked bookId
                        val review = ReviewEntity(
                            bookId = bookId,
                            dateReviewed = currentDate,
                            reviewText = reviewText.ifBlank { null }
                        )
                        reviewViewModel.addReview(review)

                        // 3. Clear and close
                        title = ""
                        authors = ""
                        reviewText = ""
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Submit")
            }
        }
    }
}

private fun generateUniqueTitle(title: String, books: List<BookEntity>): String {
    val existingTitles = books.map { it.title }
    var actualTitle = title.ifBlank { "Book1" }
    var index = 2
    while (actualTitle in existingTitles) {
        actualTitle = "Book$index"
        index++
    }
    return actualTitle
}
