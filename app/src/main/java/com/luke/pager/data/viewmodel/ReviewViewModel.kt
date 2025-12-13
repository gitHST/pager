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
    private val _reviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val reviews: StateFlow<List<ReviewEntity>> get() = _reviews

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> get() = _lastError

    init {
        loadAllReviews()
    }

    fun loadAllReviews() {
        viewModelScope.launch {
            reviewRepository
                .getAllReviews()
                .onSuccess { list -> _reviews.value = list }
                .onFailure { e ->
                    _reviews.value = emptyList()
                    _lastError.value = e.message ?: "Failed to load reviews"
                }
        }
    }

    fun deleteReviewAndBookById(reviewId: String) {
        viewModelScope.launch {
            val res = reviewRepository.deleteReviewAndBookById(reviewId)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to delete review"
                return@launch
            }
            loadAllReviews()
        }
    }

    fun updateReviewText(
        reviewId: String,
        newText: String,
    ) {
        viewModelScope.launch {
            val res = reviewRepository.updateReviewText(reviewId, newText)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to update review"
                return@launch
            }
            loadAllReviews()
        }
    }

    fun updateReviewRating(
        reviewId: String,
        newRating: Float,
    ) {
        viewModelScope.launch {
            val res = reviewRepository.updateReviewRating(reviewId, newRating)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to update rating"
                return@launch
            }
            loadAllReviews()
        }
    }

    fun updateReviewPrivacy(
        reviewId: String,
        privacy: Privacy,
    ) {
        viewModelScope.launch {
            val res = reviewRepository.updateReviewPrivacy(reviewId, privacy)
            if (res.isFailure) {
                _lastError.value = res.exceptionOrNull()?.message ?: "Failed to update privacy"
                return@launch
            }
            loadAllReviews()
        }
    }
}
