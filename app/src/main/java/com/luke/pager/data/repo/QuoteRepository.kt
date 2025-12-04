package com.luke.pager.data.repo

import com.luke.pager.data.dao.QuoteDao
import com.luke.pager.data.entities.QuoteEntity

class QuoteRepository(private val quoteDao: QuoteDao) {

    suspend fun getQuotesByBookId(bookId: Long): List<QuoteEntity> {
        return quoteDao.getQuotesByBookId(bookId)
    }

    suspend fun insertQuote(quote: QuoteEntity) {
        quoteDao.insertQuote(quote)
    }

    suspend fun getAllQuotes(): List<QuoteEntity> {
        return quoteDao.getAllQuotes()
    }

    suspend fun updateQuote(quote: QuoteEntity) {
        quoteDao.updateQuote(quote)
    }
}
