package com.luke.pager.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PagerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create books table
        db.execSQL(
            "CREATE TABLE books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "authors TEXT," + // Store authors as a comma-separated list
                    "isbn TEXT," + // International Standard Book Number
                    "cover BLOB," + // Store cover as a byte array (image)
                    "publisher TEXT," +
                    "publish_date TEXT," + // Store as text (e.g., '1925')
                    "language TEXT," +
                    "subject TEXT," + // Store subjects/genres as a comma-separated list
                    "number_of_pages INTEGER," +
                    "description TEXT," +
                    "edition TEXT," +
                    "openlibrary_key TEXT," + // OpenLibrary key for the book
                    "first_publish_date TEXT," + // First publish date (e.g., '1925')
                    "bookmarked INTEGER DEFAULT 0," + // 0 for false, 1 for true
                    "genres TEXT" + // Store genres as a comma-separated list
                    ");"
        )

        // Create reviews table
        db.execSQL(
            "CREATE TABLE reviews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "book_id INTEGER, " + // Foreign key linking to books table
                    "date_started_reading TEXT," + // When the user started reading
                    "date_finished_reading TEXT," + // When the user finished reading
                    "rating INTEGER," + // Rating from 1 to 10
                    "review_text TEXT," + // Long text review
                    "tags TEXT," + // Comma-separated list of tags
                    "FOREIGN KEY(book_id) REFERENCES books(id)" + // Link to books table
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS reviews")  // Drop the reviews table if it exists
        db.execSQL("DROP TABLE IF EXISTS books")  // Drop the books table if it exists
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "pager.db"
        private const val DATABASE_VERSION = 2 // Increment the database version
    }
}
