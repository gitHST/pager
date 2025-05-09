package com.luke.pager.network

import com.google.gson.annotations.SerializedName

data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryBook>
)

data class OpenLibraryBook(
    val key: String,
    val title: String,
    @SerializedName("author_name")
    val authorName: List<String>?,
    @SerializedName("cover_i")
    val coverIndex: Int?,
    @SerializedName("first_publish_year")
    val firstPublishYear: Int?
)
