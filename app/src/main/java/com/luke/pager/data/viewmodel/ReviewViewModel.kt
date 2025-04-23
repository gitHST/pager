package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.repo.ReviewRepository
import kotlinx.coroutines.launch

class ReviewViewModel(private val reviewRepository: ReviewRepository) : ViewModel() {

    fun deleteReviewAndBookById(reviewId: Long) {
        viewModelScope.launch {
            reviewRepository.deleteReviewAndBookById(reviewId)
        }
    }

    fun updateReviewText(reviewId: Long, newText: String) {
        viewModelScope.launch {
            reviewRepository.updateReviewText(reviewId, newText)
        }
    }

    fun updateReviewRating(reviewId: Long, newRating: Float) {
        viewModelScope.launch {
            reviewRepository.updateReviewRating(reviewId, newRating)
        }
    }

}
