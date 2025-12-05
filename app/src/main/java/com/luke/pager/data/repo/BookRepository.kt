package com.luke.pager.data.repo

import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.entities.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao
) : IBookRepository {

    override fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()

    override suspend fun insertAndReturnId(book: BookEntity): Long =
        bookDao.insertAndReturnId(book)
}
