package com.luke.pager.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "authors")
    val authors: String? = null,

    @ColumnInfo(name = "isbn")
    val isbn: String? = null,

    @ColumnInfo(name = "cover")
    val cover: ByteArray? = null,

    @ColumnInfo(name = "publisher")
    val publisher: String? = null,

    @ColumnInfo(name = "publish_date")
    val publishDate: String? = null,

    @ColumnInfo(name = "language")
    val language: String? = null,

    @ColumnInfo(name = "subject")
    val subject: String? = null,

    @ColumnInfo(name = "number_of_pages")
    val numberOfPages: Int? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "edition")
    val edition: String? = null,

    @ColumnInfo(name = "openlibrary_key")
    val openlibraryKey: String? = null,

    @ColumnInfo(name = "first_publish_date")
    val firstPublishDate: String? = null,

    @ColumnInfo(name = "bookmarked")
    val bookmarked: Boolean = false,

    @ColumnInfo(name = "genres")
    val genres: String? = null,

    @ColumnInfo(name = "date_added")
    val dateAdded: String? = null
)
