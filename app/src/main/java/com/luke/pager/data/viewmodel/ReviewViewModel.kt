package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.ReviewRepository
import kotlinx.coroutines.launch

class ReviewViewModel(private val reviewRepository: ReviewRepository) : ViewModel() {

    fun addReview(review: ReviewEntity) {
        viewModelScope.launch {
            reviewRepository.insertReview(review)
        }
    }
}
