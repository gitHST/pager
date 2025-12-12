package com.luke.pager.data.viewmodel

import Privacy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.IBookRepository
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.network.OpenLibraryBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(
    private val bookRepository: IBookRepository,
    private val reviewRepository: IReviewRepository,
) : ViewModel() {
    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> get() = _books

    private val _allReviews = MutableStateFlow<Map<String, ReviewEntity?>>(emptyMap())
    val allReviews: StateFlow<Map<String, ReviewEntity?>> get() = _allReviews

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> get() = _isInitialLoading

    private val _lastError = MutableStateFlow<String?>(null)

    val booksSortedByReviewDate: StateFlow<List<BookEntity>> =
        combine(_books, _allReviews) { books, reviewsMap ->
            val reviewsByBookId =
                reviewsMap.values
                    .filterNotNull()
                    .associateBy { it.bookId }

            books.sortedByDescending { book ->
                reviewsByBookId[book.id]?.dateReviewed
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    fun loadBooks() {
        viewModelScope.launch {
            bookRepository.getAllBooks().collect { result ->
                result
                    .onSuccess { books ->
                        _books.value = books
                    }.onFailure { e ->
                        _books.value = emptyList()
                        _lastError.value = e.message ?: "Failed to load books"
                    }

                _isInitialLoading.value = false
            }
        }
    }

    fun loadAllReviews() {
        viewModelScope.launch {
            reviewRepository
                .getAllReviews()
                .onSuccess { reviews ->
                    _allReviews.value = reviews.associateBy { it.id }
                }.onFailure { e ->
                    _allReviews.value = emptyMap()
                    _lastError.value = e.message ?: "Failed to load reviews"
                }
        }
    }

    fun submitReview(
        openBook: OpenLibraryBook,
        rating: Float?,
        reviewText: String,
        dateReviewed: String,
        privacy: Privacy,
        hasSpoilers: Boolean,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val book =
                BookEntity(
                    title = openBook.title,
                    authors = openBook.authorName?.joinToString(),
                    openlibraryKey = openBook.key,
                    firstPublishDate = openBook.firstPublishYear?.toString(),
                    coverId = openBook.coverIndex,
                )

            val bookIdResult = bookRepository.insertAndReturnId(book)
            val bookId =
                bookIdResult.getOrElse { e ->
                    _lastError.value = e.message ?: "Failed to save book"
                    return@launch
                }

            val sanitizedReviewText = reviewText.takeIf { it.isNotBlank() }

            val review =
                ReviewEntity(
                    bookId = bookId,
                    bookKey = openBook.key,
                    rating = rating,
                    reviewText = sanitizedReviewText,
                    dateReviewed = dateReviewed,
                    privacy = privacy,
                    hasSpoilers = hasSpoilers,
                )

            val reviewResult = reviewRepository.insertReview(review)
            if (reviewResult.isFailure) {
                _lastError.value = reviewResult.exceptionOrNull()?.message ?: "Failed to save review"
                return@launch
            }

            loadBooks()
            loadAllReviews()
            onSuccess()
        }
    }
}
