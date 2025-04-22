package com.luke.pager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luke.pager.data.entities.ReviewEntity

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReturnId(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews WHERE book_id = :bookId LIMIT 1")
    suspend fun getReviewByBookId(bookId: Long): ReviewEntity?

    @Query("UPDATE reviews SET review_text = :newText WHERE id = :reviewId")
    suspend fun updateReviewText(reviewId: Long, newText: String)

    @Delete
    suspend fun deleteReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviews(): List<ReviewEntity>

    @Query("SELECT book_id FROM reviews WHERE id = :reviewId")
    suspend fun getBookIdByReviewId(reviewId: Long): Long?
}
