package com.luke.pager.network

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("title") title: String? = null,
        @Query("author") author: String? = null,
    ): OpenLibrarySearchResponse
}
