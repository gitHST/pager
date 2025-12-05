package com.luke.pager.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.dao.QuoteDao
import com.luke.pager.data.dao.ReviewDao
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.entities.ReviewEntity

@Database(entities = [BookEntity::class, ReviewEntity::class, QuoteEntity::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun reviewDao(): ReviewDao
    abstract fun quoteDao(): QuoteDao
}
