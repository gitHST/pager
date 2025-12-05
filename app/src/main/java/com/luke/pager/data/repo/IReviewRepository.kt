package com.luke.pager.data.repo

import Privacy
import com.luke.pager.data.entities.ReviewEntity

interface IReviewRepository {
    suspend fun insertReview(review: ReviewEntity)
    suspend fun getAllReviews(): List<ReviewEntity>
    suspend fun deleteReviewAndBookById(reviewId: Long)

    suspend fun updateReviewText(
        reviewId: Long,
        newText: String
    )

    suspend fun updateReviewRating(
        reviewId: Long,
        newRating: Float
    )

    suspend fun updateReviewPrivacy(
        reviewId: Long,
        privacy: Privacy
    )
}
