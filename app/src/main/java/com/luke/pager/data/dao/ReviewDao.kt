package com.luke.pager.data.dao

import Privacy
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luke.pager.data.entities.ReviewEntity

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("UPDATE reviews SET review_text = :newText WHERE id = :reviewId")
    suspend fun updateReviewText(
        reviewId: Long,
        newText: String
    )

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviews(): List<ReviewEntity>

    @Query("SELECT book_id FROM reviews WHERE id = :reviewId")
    suspend fun getBookIdByReviewId(reviewId: Long): Long?

    @Query("UPDATE reviews SET rating = :newRating WHERE id = :reviewId")
    suspend fun updateReviewRating(
        reviewId: Long,
        newRating: Float
    )

    @Query("UPDATE reviews SET privacy = :newPrivacy WHERE id = :reviewId")
    suspend fun updateReviewPrivacy(
        reviewId: Long,
        newPrivacy: Privacy
    )
}
