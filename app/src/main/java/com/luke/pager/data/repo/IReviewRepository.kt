package com.luke.pager.data.repo

import Privacy
import com.luke.pager.data.entities.ReviewEntity

interface IReviewRepository {
    suspend fun insertReview(review: ReviewEntity): Result<Unit>

    suspend fun getAllReviews(): Result<List<ReviewEntity>>

    suspend fun deleteReviewAndBookById(reviewId: String): Result<Unit>

    suspend fun updateReviewText(
        reviewId: String,
        newText: String,
    ): Result<Unit>

    suspend fun updateReviewRating(
        reviewId: String,
        newRating: Float,
    ): Result<Unit>

    suspend fun updateReviewPrivacy(
        reviewId: String,
        privacy: Privacy,
    ): Result<Unit>
}
