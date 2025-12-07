package com.luke.pager.data.entities

data class BookEntity(
    val id: String = "",
    val title: String,
    val authors: String? = null,
    val isbn: String? = null,
    val cover: ByteArray? = null,
    val coverId: Int? = null,
    val publisher: String? = null,
    val publishDate: String? = null,
    val language: String? = null,
    val subject: String? = null,
    val numberOfPages: Int? = null,
    val description: String? = null,
    val edition: String? = null,
    val openlibraryKey: String? = null,
    val firstPublishDate: String? = null,
    val bookmarked: Boolean = false,
    val genres: String? = null,
    val dateAdded: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookEntity

        if (coverId != other.coverId) return false
        if (numberOfPages != other.numberOfPages) return false
        if (bookmarked != other.bookmarked) return false
        if (id != other.id) return false
        if (title != other.title) return false
        if (authors != other.authors) return false
        if (isbn != other.isbn) return false
        if (cover != null && other.cover != null) {
            if (!cover.contentEquals(other.cover)) return false
        } else if (!cover.contentEquals(other.cover)) {
            // one null, one non-null
            return false
        }
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
        var result = coverId ?: 0
        result = 31 * result + (numberOfPages ?: 0)
        result = 31 * result + bookmarked.hashCode()
        result = 31 * result + id.hashCode()
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
