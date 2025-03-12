package com.luke.pager.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class BookDao(private val dbHelper: PagerDatabaseHelper) {

    fun insertBook(title: String, description: String?, cover: ByteArray?, rating: Float): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("description", description)
            put("cover", cover)
            put("rating", rating)
        }
        return db.insert("books", null, values)
    }

    fun getAllBooks(): Cursor {
        val db = dbHelper.readableDatabase
        return db.rawQuery("SELECT * FROM books ORDER BY id DESC", null)
    }
}
