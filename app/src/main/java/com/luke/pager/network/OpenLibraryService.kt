package com.luke.pager.network

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryBook>
)

data class OpenLibraryBook(
    val key: String,  // e.g. "/works/OL12345W"
    val title: String,
    val author_name: List<String>?,
    val cover_i: Int?,  // cover image id
    val first_publish_year: Int?
)