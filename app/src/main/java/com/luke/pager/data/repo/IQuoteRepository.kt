package com.luke.pager.data.repo

import com.luke.pager.data.entities.QuoteEntity

interface IQuoteRepository {
    suspend fun getQuotesByBookId(bookId: String): Result<List<QuoteEntity>>

    suspend fun insertQuote(quote: QuoteEntity): Result<Unit>

    suspend fun getAllQuotes(): Result<List<QuoteEntity>>

    suspend fun updateQuote(quote: QuoteEntity): Result<Unit>

    suspend fun deleteQuote(quote: QuoteEntity): Result<Unit>
}
