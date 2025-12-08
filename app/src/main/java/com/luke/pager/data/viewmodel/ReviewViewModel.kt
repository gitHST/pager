package com.luke.pager.data.viewmodel

import Privacy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.repo.IReviewRepository
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: IReviewRepository,
) : ViewModel() {
    fun deleteReviewAndBookById(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.deleteReviewAndBookById(reviewId)
        }
    }

    fun updateReviewText(
        reviewId: String,
        newText: String,
    ) {
        viewModelScope.launch {
            reviewRepository.updateReviewText(reviewId, newText)
        }
    }

    fun updateReviewRating(
        reviewId: String,
        newRating: Float,
    ) {
        viewModelScope.launch {
            reviewRepository.updateReviewRating(reviewId, newRating)
        }
    }

    fun updateReviewPrivacy(
        reviewId: String,
        privacy: Privacy,
    ) {
        viewModelScope.launch {
            reviewRepository.updateReviewPrivacy(reviewId, privacy)
        }
    }
}
