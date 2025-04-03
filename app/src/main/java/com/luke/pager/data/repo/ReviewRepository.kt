package com.luke.pager.data.repo

import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.dao.ReviewDao

class ReviewRepository(private val reviewDao: ReviewDao) {
    fun getReviewsForBook(bookId: Int) = reviewDao.getReviewsForBook(bookId)
    suspend fun insertReview(review: ReviewEntity) = reviewDao.insertReview(review)
    suspend fun deleteReview(review: ReviewEntity) = reviewDao.deleteReview(review)
}