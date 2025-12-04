package com.luke.pager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.luke.pager.data.entities.QuoteEntity

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes WHERE book_id = :bookId ORDER BY date_added DESC")
    suspend fun getQuotesByBookId(bookId: Long): List<QuoteEntity>

    @Insert
    suspend fun insertQuote(quote: QuoteEntity)

    @Query("SELECT * FROM quotes ORDER BY date_added DESC")
    suspend fun getAllQuotes(): List<QuoteEntity>

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

}
