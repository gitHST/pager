package com.luke.pager.data.viewmodel

import com.luke.pager.data.entities.ReviewEntity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.repo.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(private val reviewRepository: ReviewRepository) : ViewModel() {

    fun getReviews(bookId: Int): LiveData<List<ReviewEntity>> {
        return reviewRepository.getReviewsForBook(bookId).asLiveData()
    }

    fun addReview(review: ReviewEntity) {
        viewModelScope.launch {
            reviewRepository.insertReview(review)
        }
    }

    fun deleteReview(review: ReviewEntity) {
        viewModelScope.launch {
            reviewRepository.deleteReview(review)
        }
    }
}
