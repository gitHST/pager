package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.repo.IQuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuoteViewModel(
    private val quoteRepository: IQuoteRepository,
) : ViewModel() {
    private val _quotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    private val _allQuotes = MutableStateFlow<List<QuoteEntity>>(emptyList())

    val allQuotes: StateFlow<List<QuoteEntity>> = _allQuotes
    val quotes: StateFlow<List<QuoteEntity>> = _quotes

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> get() = _lastError

    fun loadQuotesForBook(bookId: String) {
        viewModelScope.launch {
            quoteRepository
                .getQuotesByBookId(bookId)
                .onSuccess { _quotes.value = it }
                .onFailure { e ->
                    _quotes.value = emptyList()
                    _lastError.value = e.message ?: "Failed to load quotes"
                }
        }
    }

    fun addQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            val res = quoteRepository.insertQuote(quote)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to save quote"
                return@launch
            }
            loadQuotesForBook(quote.bookId)
            loadAllQuotes()
        }
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            quoteRepository
                .getAllQuotes()
                .onSuccess { _allQuotes.value = it }
                .onFailure { e ->
                    _allQuotes.value = emptyList()
                    _lastError.value = e.message ?: "Failed to load all quotes"
                }
        }
    }

    fun updateQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            val res = quoteRepository.updateQuote(quote)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to update quote"
                return@launch
            }
            loadQuotesForBook(quote.bookId)
            loadAllQuotes()
        }
    }

    fun deleteQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            val res = quoteRepository.deleteQuote(quote)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to delete quote"
                return@launch
            }
            loadQuotesForBook(quote.bookId)
            loadAllQuotes()
        }
    }
}
