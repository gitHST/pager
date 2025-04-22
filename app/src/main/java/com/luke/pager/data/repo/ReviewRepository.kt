package com.luke.pager.data.repo

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
}