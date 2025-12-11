package com.luke.pager.data.viewmodel

import Privacy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.IReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: IReviewRepository,
) : ViewModel() {

    // -----------------------------------------
    // NEW: expose reviews for export & UI
    // -----------------------------------------
    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> get() = _reviews

    init {
        loadAllReviews()
    }

    // -----------------------------------------
    // NEW: load all reviews from repository
    // -----------------------------------------
    fun loadAllReviews() {
        viewModelScope.launch {
            _reviews.value = reviewRepository.getAllReviews()
        }
    }

    // -----------------------------------------
    // Existing functions
    // -----------------------------------------
    fun deleteReviewAndBookById(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.deleteReviewAndBookById(reviewId)

            // Keep state in sync
            loadAllReviews()
        }
    }

    fun updateReviewText(
        reviewId: String,
        newText: String,
    ) {
        viewModelScope.launch {
            reviewRepository.updateReviewText(reviewId, newText)
            loadAllReviews()
        }
    }

    fun updateReviewRating(
        reviewId: String,
        newRating: Float,
    ) {
        viewModelScope.launch {
            reviewRepository.updateReviewRating(reviewId, newRating)
            loadAllReviews()
        }
    }

    fun updateReviewPrivacy(
        reviewId: String,
        privacy: Privacy,
    ) {
        viewModelScope.launch {
            reviewRepository.updateReviewPrivacy(reviewId, privacy)
            loadAllReviews()
        }
    }
}
