package com.luke.pager.network.apiresponse

import com.luke.pager.network.OpenLibraryApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenLibraryService {
    val api: OpenLibraryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenLibraryApi::class.java)
    }
}