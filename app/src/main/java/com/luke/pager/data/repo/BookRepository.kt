package com.luke.pager.data.repo

import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.dao.BookDao

class BookRepository(private val bookDao: BookDao) {
    fun getAllBooks() = bookDao.getAllBooks()
    suspend fun insertBook(book: BookEntity) = bookDao.insertBook(book)
    suspend fun deleteBook(book: BookEntity) = bookDao.deleteBook(book)
}