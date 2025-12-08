package com.luke.pager.data.entities

import Privacy

data class ReviewEntity(
    val id: String = "",
    val bookId: String,
    val bookKey: String? = null,
    val dateStartedReading: String? = null,
    val dateFinishedReading: String? = null,
    val dateReviewed: String? = null,
    val rating: Float? = null,
    val reviewText: String? = null,
    val tags: String? = null,
    val privacy: Privacy = Privacy.PUBLIC,
    val hasSpoilers: Boolean = false,
)
