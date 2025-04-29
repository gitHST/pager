package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.repo.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuoteViewModel(private val quoteRepository: QuoteRepository) : ViewModel() {
    private val _quotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    private val _allQuotes = MutableStateFlow<List<QuoteEntity>>(emptyList())
    val allQuotes: StateFlow<List<QuoteEntity>> = _allQuotes
    val quotes: StateFlow<List<QuoteEntity>> = _quotes

    fun loadQuotesForBook(bookId: Long) {
        viewModelScope.launch {
            _quotes.value = quoteRepository.getQuotesByBookId(bookId)
        }
    }
    fun addQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            quoteRepository.insertQuote(quote)
            loadQuotesForBook(quote.bookId)
            loadAllQuotes()
        }
    }
    fun loadAllQuotes() {
        viewModelScope.launch {
            _allQuotes.value = quoteRepository.getAllQuotes()
        }
    }
}
