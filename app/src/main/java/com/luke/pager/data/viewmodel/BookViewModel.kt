package com.luke.pager.data.viewmodel

import Privacy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.IBookRepository
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.network.OpenLibraryBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class BookViewModel(
    private val bookRepository: IBookRepository,
    private val reviewRepository: IReviewRepository
) : ViewModel() {

    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> get() = _books

    private val _allReviews = MutableStateFlow<Map<Long, ReviewEntity?>>(emptyMap())
    val allReviews: StateFlow<Map<Long, ReviewEntity?>> get() = _allReviews

    val booksSortedByReviewDate: StateFlow<List<BookEntity>> =
        combine(_books, _allReviews) { books, reviews ->
            books.sortedByDescending { book ->
                reviews[book.id]?.dateReviewed
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    suspend fun insertAndReturnId(book: BookEntity): Long {
        return bookRepository.insertAndReturnId(book)
    }

    suspend fun downloadCoverImage(coverId: Int?): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                coverId?.let {
                    val url = URL("https://covers.openlibrary.org/b/id/$it-M.jpg")
                    url.readBytes()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            bookRepository.getAllBooks().collect {
                _books.value = it
            }
        }
    }

    fun loadAllReviews() {
        viewModelScope.launch {
            val reviews = reviewRepository.getAllReviews()
            _allReviews.value = reviews.associateBy { it.bookId }
        }
    }

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
            val coverImage = downloadCoverImage(openBook.coverIndex)

            val book =
                BookEntity(
                    title = openBook.title,
                    authors = openBook.authorName?.joinToString(),
                    openlibraryKey = openBook.key,
                    firstPublishDate = openBook.firstPublishYear?.toString(),
                    cover = coverImage
                )

            val bookId = insertAndReturnId(book)

            val sanitizedReviewText = reviewText.takeIf { it.isNotBlank() }

            val review =
                ReviewEntity(
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
