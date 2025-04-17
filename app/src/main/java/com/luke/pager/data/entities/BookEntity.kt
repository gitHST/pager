package com.luke.pager.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookEntity

        if (id != other.id) return false
        if (numberOfPages != other.numberOfPages) return false
        if (bookmarked != other.bookmarked) return false
        if (title != other.title) return false
        if (authors != other.authors) return false
        if (isbn != other.isbn) return false
        if (!cover.contentEquals(other.cover)) return false
        if (publisher != other.publisher) return false
        if (publishDate != other.publishDate) return false
        if (language != other.language) return false
        if (subject != other.subject) return false
        if (description != other.description) return false
        if (edition != other.edition) return false
        if (openlibraryKey != other.openlibraryKey) return false
        if (firstPublishDate != other.firstPublishDate) return false
        if (genres != other.genres) return false
        if (dateAdded != other.dateAdded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (numberOfPages ?: 0)
        result = 31 * result + bookmarked.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (authors?.hashCode() ?: 0)
        result = 31 * result + (isbn?.hashCode() ?: 0)
        result = 31 * result + (cover?.contentHashCode() ?: 0)
        result = 31 * result + (publisher?.hashCode() ?: 0)
        result = 31 * result + (publishDate?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (subject?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (edition?.hashCode() ?: 0)
        result = 31 * result + (openlibraryKey?.hashCode() ?: 0)
        result = 31 * result + (firstPublishDate?.hashCode() ?: 0)
        result = 31 * result + (genres?.hashCode() ?: 0)
        result = 31 * result + (dateAdded?.hashCode() ?: 0)
        return result
    }
}
