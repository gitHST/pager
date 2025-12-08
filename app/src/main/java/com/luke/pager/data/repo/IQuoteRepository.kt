package com.luke.pager.data.repo

import com.luke.pager.data.entities.QuoteEntity

interface IQuoteRepository {
    suspend fun getQuotesByBookId(bookId: String): List<QuoteEntity>

    suspend fun insertQuote(quote: QuoteEntity)

    suspend fun getAllQuotes(): List<QuoteEntity>

    suspend fun updateQuote(quote: QuoteEntity)

    suspend fun deleteQuote(quote: QuoteEntity)
}
