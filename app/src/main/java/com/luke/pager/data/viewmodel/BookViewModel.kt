package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.BookRepository
import com.luke.pager.data.repo.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookViewModel(private val bookRepository: BookRepository, private val reviewRepository: ReviewRepository) : ViewModel() {

    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> get() = _books

    private val _allReviews = MutableStateFlow<Map<Long, ReviewEntity?>>(emptyMap())
    val allReviews: StateFlow<Map<Long, ReviewEntity?>> get() = _allReviews

    suspend fun insertAndReturnId(book: BookEntity): Long {
        return bookRepository.insertAndReturnId(book)
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

}
