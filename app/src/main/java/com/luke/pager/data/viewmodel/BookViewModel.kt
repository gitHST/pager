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
    private val reviewRepository: IReviewRepository
) : ViewModel() {

    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> get() = _books

    private val _allReviews = MutableStateFlow<Map<String, ReviewEntity?>>(emptyMap())
    val allReviews: StateFlow<Map<String, ReviewEntity?>> get() = _allReviews

    val booksSortedByReviewDate: StateFlow<List<BookEntity>> =
        combine(_books, _allReviews) { books, reviews ->
            books.sortedByDescending { book ->
                reviews[book.id]?.dateReviewed
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    /**
     * Insert a book and return its Firestore auto-generated string ID.
     */
    suspend fun insertAndReturnId(book: BookEntity): String {
        return bookRepository.insertAndReturnId(book)
    }

    fun loadBooks() {
        viewModelScope.launch {
            bookRepository.getAllBooks().collect { books ->
                _books.value = books
            }
        }
    }

    fun loadAllReviews() {
        viewModelScope.launch {
            val reviews = reviewRepository.getAllReviews()
            _allReviews.value = reviews.associateBy { it.bookId } // bookId is String
        }
    }

    /**
     * Create a new Book + Review pair for an OpenLibrary result.
     * - Book gets a Firestore auto ID (string)
     * - Review uses that ID as bookId
     */
    fun submitReview(
        openBook: OpenLibraryBook,
        rating: Float?,
        reviewText: String,
        dateReviewed: String,
        privacy: Privacy,
        hasSpoilers: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val book =
                BookEntity(
                    // id is left as default "", FirebaseBookRepository will replace it
                    title = openBook.title,
                    authors = openBook.authorName?.joinToString(),
                    openlibraryKey = openBook.key,
                    firstPublishDate = openBook.firstPublishYear?.toString(),
                    coverId = openBook.coverIndex
                )

            // Firestore auto-ID from repository (string)
            val bookId: String = insertAndReturnId(book)

            val sanitizedReviewText = reviewText.takeIf { it.isNotBlank() }

            val review =
                ReviewEntity(
                    // id left as default "", FirebaseReviewRepository will assign auto-ID
                    bookId = bookId,
                    rating = rating,
                    reviewText = sanitizedReviewText,
                    dateReviewed = dateReviewed,
                    privacy = privacy,
                    hasSpoilers = hasSpoilers
                )

            reviewRepository.insertReview(review)
            loadBooks()
            loadAllReviews()
            onSuccess()
        }
    }
}
