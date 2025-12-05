package com.luke.pager.data.repo

import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.entities.BookEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BookRepository(
    private val bookDao: BookDao
) : IBookRepository {

    override fun getAllBooks(): Flow<List<BookEntity>> =
        bookDao.getAllBooks()

    override suspend fun insertAndReturnId(book: BookEntity): String {
        val id =
            book.id.ifBlank {
                UUID.randomUUID().toString()
            }

        val bookToInsert = book.copy(id = id)
        bookDao.insertBook(bookToInsert)
        return id
    }
}
