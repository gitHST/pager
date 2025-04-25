package com.luke.pager.data.repo

import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.entities.BookEntity

class BookRepository(private val bookDao: BookDao) {
    fun getAllBooks() = bookDao.getAllBooks()

    suspend fun insertAndReturnId(book: BookEntity) = bookDao.insertAndReturnId(book)
}
