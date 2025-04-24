package com.luke.pager.network

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryBook>
)

data class OpenLibraryBook(
    val key: String,
    val title: String,
    val author_name: List<String>?,
    val cover_i: Int?,
    val first_publish_year: Int?
)