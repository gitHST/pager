package com.luke.pager.data.repo

import com.luke.pager.data.dao.QuoteDao
import com.luke.pager.data.entities.QuoteEntity

class QuoteRepository(
    private val quoteDao: QuoteDao
) : IQuoteRepository {

    override suspend fun getQuotesByBookId(bookId: Long): List<QuoteEntity> {
        return quoteDao.getQuotesByBookId(bookId)
    }

    override suspend fun insertQuote(quote: QuoteEntity) {
        quoteDao.insertQuote(quote)
    }

    override suspend fun getAllQuotes(): List<QuoteEntity> {
        return quoteDao.getAllQuotes()
    }

    override suspend fun updateQuote(quote: QuoteEntity) {
        quoteDao.updateQuote(quote)
    }

    override suspend fun deleteQuote(quote: QuoteEntity) {
        quoteDao.deleteQuote(quote)
    }
}
