package com.luke.pager.data.repo

import Privacy
import com.luke.pager.data.entities.ReviewEntity

interface IReviewRepository {
    suspend fun insertReview(review: ReviewEntity)
    suspend fun getAllReviews(): List<ReviewEntity>

    suspend fun deleteReviewAndBookById(reviewId: String)
    suspend fun updateReviewText(reviewId: String, newText: String)
    suspend fun updateReviewRating(reviewId: String, newRating: Float)
    suspend fun updateReviewPrivacy(reviewId: String, privacy: Privacy)
}
