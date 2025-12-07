package com.luke.pager.data.entities

data class QuoteEntity(
    val id: String = "",
    val bookId: String,
    val quoteText: String,
    val pageNumber: Int? = null,
    val dateAdded: String? = null
)
