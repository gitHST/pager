package com.luke.pager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.luke.pager.data.entities.ReviewEntity

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReturnId(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews WHERE book_id = :bookId LIMIT 1")
    suspend fun getReviewByBookId(bookId: Long): ReviewEntity?

    @Update
    suspend fun updateReview(review: ReviewEntity)

    @Delete
    suspend fun deleteReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviews(): List<ReviewEntity>
}
