package com.luke.pager.data.entities

import Privacy
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["book_id"])]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Long,

    @ColumnInfo(name = "date_started_reading")
    val dateStartedReading: String? = null,

    @ColumnInfo(name = "date_finished_reading")
    val dateFinishedReading: String? = null,

    @ColumnInfo(name = "date_reviewed")
    val dateReviewed: String? = null,

    @ColumnInfo(name = "rating")
    val rating: Float? = null,

    @ColumnInfo(name = "review_text")
    val reviewText: String? = null,

    @ColumnInfo(name = "tags")
    val tags: String? = null,

    @ColumnInfo(name = "privacy")
    val privacy: Privacy = Privacy.PUBLIC,

    @ColumnInfo(name = "has_spoilers")
    val hasSpoilers: Boolean = false
)