package com.luke.pager.data.repo

import Privacy
import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.dao.ReviewDao
import com.luke.pager.data.entities.ReviewEntity
import java.util.UUID

class ReviewRepository(
    private val reviewDao: ReviewDao,
    private val bookDao: BookDao
) : IReviewRepository {

    override suspend fun insertReview(review: ReviewEntity) {
        val id =
            if (review.id.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                review.id
            }

        val reviewToInsert = review.copy(id = id)
        reviewDao.insertReview(reviewToInsert)
    }

    override suspend fun getAllReviews(): List<ReviewEntity> =
        reviewDao.getAllReviews()

    override suspend fun deleteReviewAndBookById(reviewId: String) {
        val bookId = reviewDao.getBookIdByReviewId(reviewId)
        if (bookId != null) {
            bookDao.deleteBookById(bookId)
        }
    }

    override suspend fun updateReviewText(
        reviewId: String,
        newText: String
    ) {
        reviewDao.updateReviewText(reviewId, newText)
    }

    override suspend fun updateReviewRating(
        reviewId: String,
        newRating: Float
    ) {
        reviewDao.updateReviewRating(reviewId, newRating)
    }

    override suspend fun updateReviewPrivacy(
        reviewId: String,
        privacy: Privacy
    ) {
        reviewDao.updateReviewPrivacy(reviewId, privacy)
    }
}
