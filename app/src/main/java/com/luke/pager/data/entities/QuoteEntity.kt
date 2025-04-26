package com.luke.pager.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quotes",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "book_id", index = true)
    val bookId: Long,
    @ColumnInfo(name = "quote_text")
    val quoteText: String,
    @ColumnInfo(name = "page_number")
    val pageNumber: Int? = null,
    @ColumnInfo(name = "date_added")
    val dateAdded: String? = null
)
