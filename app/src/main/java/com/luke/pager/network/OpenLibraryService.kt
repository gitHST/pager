package com.luke.pager.network

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryBook>
)

data class OpenLibraryBook(
    val key: String,
    val title: String,
    val authorName: List<String>?,
    val coverIndex: Int?,
    val firstPublishYear: Int?
)