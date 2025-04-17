package com.luke.pager.data.repo

import com.luke.pager.data.dao.ReviewDao
import com.luke.pager.data.entities.ReviewEntity

class ReviewRepository(private val reviewDao: ReviewDao) {
    suspend fun insertReview(review: ReviewEntity) = reviewDao.insertReview(review)
    suspend fun getAllReviews() = reviewDao.getAllReviews()
}