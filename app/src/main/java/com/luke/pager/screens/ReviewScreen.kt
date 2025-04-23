package com.luke.pager.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.addscreen.PrivacyToggle
import com.luke.pager.screens.addscreen.StarRatingBar

@Composable
fun ReviewScreen(
    reviewId: Long,
    reviews: Map<Long, ReviewEntity?>,
    reviewViewModel: ReviewViewModel,
    onDeleteSuccess: () -> Unit
) {
    val review = reviews[reviewId]
    var showDeleteDialog by remember { mutableStateOf(false) }

    review?.let {
        var isEditing by remember { mutableStateOf(false) }
        var localReviewText by remember { mutableStateOf(review.reviewText.orEmpty()) }
        var editedText by remember { mutableStateOf(TextFieldValue(localReviewText)) }
        var menuExpanded by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        var localRating by remember { mutableFloatStateOf(review.rating?.toFloat() ?: 0f) }
        var tempDisplayRating by remember { mutableFloatStateOf(review.rating?.toFloat() ?: 0f) }
        var localPrivacy by remember { mutableStateOf(review.privacy) }
        val textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )

        LaunchedEffect(isEditing) {
            if (isEditing) {
                editedText = editedText.copy(selection = TextRange(editedText.text.length))
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }


        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with menu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Review",
                        fontSize = 24.sp
                    )

                    if (!isEditing) {
                        Box(modifier = Modifier.align(Alignment.TopEnd)) {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options"
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier
                                    .padding(top = 0.dp)
                                    .background(Color.White, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        menuExpanded = false
                                        isEditing = true
                                        editedText = TextFieldValue(localReviewText)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (!isEditing) {
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    PrivacyToggle(privacy = localPrivacy, onLockToggle = { localPrivacy = it })
                }

                // Date
                review.dateReviewed?.let {
                    val dateOnly = it.split(" ").firstOrNull() ?: it
                    Text("Finished reading on: $dateOnly", fontSize = 14.sp)
                }

                // Rating Stars
                if (isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingBar(
                        rating = localRating,
                        hasRated = true,
                        onRatingChange = { newRating: Float -> localRating = newRating },
                        onUserInteracted = {},
                        starScale = 1.0f,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                } else {
                    if (tempDisplayRating > 0f) {
                        val rating = tempDisplayRating
                        val starScale = 1.5f
                        val starSize = 24.dp * starScale
                        val starRowWidthFraction = 0.7f

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(starRowWidthFraction)
                                .height(starSize)
                        ) {
                            Row(modifier = Modifier.matchParentSize()) {
                                for (i in 1..5) {
                                    val icon = when {
                                        rating >= i -> Icons.Filled.Star
                                        rating == i - 0.5f -> Icons.Outlined.StarHalf
                                        else -> Icons.Outlined.StarBorder
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.height(starSize)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                // Review Text / Edit
                val sharedModifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)

                if (isEditing) {
                    Box(modifier = sharedModifier) {
                        BasicTextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            textStyle = textStyle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )

                    }
                } else {
                    Box(modifier = sharedModifier) {
                        Text(
                            localReviewText.ifBlank { "No review given" },
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth(),
                            style = textStyle,
                            fontStyle = if (localReviewText.isBlank()) FontStyle.Italic else FontStyle.Normal
                        )
                    }
                }
            }

            // Floating Save/Cancel Buttons
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        reviewViewModel.updateReviewText(reviewId, editedText.text)
                        reviewViewModel.updateReviewRating(reviewId, localRating)
                        reviewViewModel.updateReviewPrivacy(reviewId, localPrivacy)
                        localReviewText = editedText.text
                        tempDisplayRating = localRating
                        isEditing = false
                    }) {
                        Text("Save")
                    }
                    Button(onClick = {
                        isEditing = false
                        editedText = TextFieldValue(localReviewText)
                    }) {
                        Text("Cancel")
                    }
                }
            }

            // Delete Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                reviewViewModel.deleteReviewAndBookById(reviewId)
                                onDeleteSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Delete review") },
                    text = { Text("Delete this review?") }
                )
            }
        }
    }
}
