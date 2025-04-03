package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.repo.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(private val bookRepository: BookRepository) : ViewModel() {

    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())

    val books: StateFlow<List<BookEntity>> get() = _books

    fun addBook(book: BookEntity) {
        viewModelScope.launch {
            bookRepository.insertBook(book)
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            bookRepository.deleteBook(book)
        }
    }

    fun getBooks() {
        viewModelScope.launch {
            bookRepository.getAllBooks()
        }
    }
}
