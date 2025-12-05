package com.luke.pager.data.repo

import com.luke.pager.data.dao.QuoteDao
import com.luke.pager.data.entities.QuoteEntity
import java.util.UUID

class QuoteRepository(
    private val quoteDao: QuoteDao
) : IQuoteRepository {

    override suspend fun getQuotesByBookId(bookId: String): List<QuoteEntity> {
        return quoteDao.getQuotesByBookId(bookId)
    }

    override suspend fun insertQuote(quote: QuoteEntity) {
        val id =
            quote.id.ifBlank {
                UUID.randomUUID().toString()
            }

        val quoteToInsert = quote.copy(id = id)
        quoteDao.insertQuote(quoteToInsert)
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
