package com.luke.pager.data.repo

import Privacy
import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.dao.ReviewDao
import com.luke.pager.data.entities.ReviewEntity

class ReviewRepository(private val reviewDao: ReviewDao, private val bookDao: BookDao) {
    suspend fun insertReview(review: ReviewEntity) = reviewDao.insertReview(review)

    suspend fun getAllReviews() = reviewDao.getAllReviews()

    suspend fun deleteReviewAndBookById(reviewId: Long) {
        val bookId = reviewDao.getBookIdByReviewId(reviewId)
        bookId?.let {
            bookDao.deleteBookById(it)
        }
    }

    suspend fun updateReviewText(
        reviewId: Long,
        newText: String
    ) {
        reviewDao.updateReviewText(reviewId, newText)
    }

    suspend fun updateReviewRating(
        reviewId: Long,
        newRating: Float
    ) {
        reviewDao.updateReviewRating(reviewId, newRating)
    }

    suspend fun updateReviewPrivacy(
        reviewId: Long,
        privacy: Privacy
    ) {
        reviewDao.updateReviewPrivacy(reviewId, privacy)
    }
}
