package com.luke.pager.ui

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import com.luke.pager.data.BookDao
import com.luke.pager.data.PagerDatabaseHelper

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val bookDao = BookDao(PagerDatabaseHelper(application))

    fun addBook(title: String, description: String?, cover: ByteArray?, rating: Float) {
        bookDao.insertBook(title, description, cover, rating)
    }

    fun getBooks(): Cursor {
        return bookDao.getAllBooks()
    }
}
