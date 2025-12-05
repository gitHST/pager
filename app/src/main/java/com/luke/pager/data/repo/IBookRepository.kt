package com.luke.pager.data.repo

import com.luke.pager.data.entities.BookEntity
import kotlinx.coroutines.flow.Flow

interface IBookRepository {
    fun getAllBooks(): Flow<List<BookEntity>>

    // 🔥 was Long, now String
    suspend fun insertAndReturnId(book: BookEntity): String
}
