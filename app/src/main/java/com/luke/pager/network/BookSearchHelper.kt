package com.luke.pager.network

import retrofit2.HttpException
import java.io.IOException


data class SearchResult(
    val books: List<OpenLibraryBook>,
    val errorMessage: String? = null
)



suspend fun searchBooksSmart(rawQuery: String): SearchResult {
    val query = rawQuery.trim()
    val lowerQuery = query.lowercase()

    val titleResponse: OpenLibrarySearchResponse
    val authorResponse: OpenLibrarySearchResponse

    try {
        titleResponse = OpenLibraryService.api.searchBooks(title = query)
        authorResponse = OpenLibraryService.api.searchBooks(author = query)
    } catch (e: HttpException) {
        return if (e.code() == 503) {
            SearchResult(emptyList(), "Server is currently unavailable.")
        } else {
            SearchResult(emptyList(), "An unexpected HTTP error occurred.")
        }
    } catch (_: IOException) {
        return SearchResult(emptyList(), "Network error. Please check your connection.")
    } catch (e: Exception) {
        return SearchResult(emptyList(), "Unknown error: ${e.localizedMessage ?: "No message"}")
    }



    val combined = (titleResponse.docs + authorResponse.docs)
    val seen = mutableMapOf<Pair<String, String>, OpenLibraryBook>()
    val result = mutableListOf<Pair<Int, OpenLibraryBook>>()

    for (book in combined) {
        val normTitle = book.title.normalizeTitle()
        val strippedTitle = normTitle.stripLeadingArticle()
        val author = book.author_name?.firstOrNull()?.lowercase() ?: continue
        val key = strippedTitle to author
        val existing = seen[key]
        val keepThis = existing == null || normTitle.length > existing.title.normalizeTitle().length
        if (keepThis) seen[key] = book
    }

    for ((_, book) in seen) {
        val titleLower = book.title.lowercase()
        val authorMatch = book.author_name?.any { it.equals(query, ignoreCase = true) } == true
        val titleMatch = titleLower.contains(lowerQuery)
        val lengthPenalty = (book.title.length - query.length).coerceAtLeast(0)
        val finalScore = when {
            authorMatch -> 300
            titleMatch -> 200 - (lengthPenalty / 3)
            else -> 100
        }
        result += finalScore to book
    }

    val sorted = result
        .sortedWith(compareByDescending<Pair<Int, OpenLibraryBook>> { it.first }
            .thenBy { it.second.title.lowercase() })
        .map { it.second }

    return SearchResult(sorted)
}




    fun String.normalizeTitle(): String {
    return this.lowercase()
        .replace(Regex("""^["'“”‘’]+|["'“”‘’]+$"""), "")   // remove leading/trailing quotes
        .replace(Regex("""\s*\(.*?\)"""), "")              // remove parentheses and content
        .replace(Regex(""":.*$"""), "")                    // remove subtitles after colon
        .replace(Regex("""-.*$"""), "")                    // remove subtitles after dash
        .replace(Regex("""\s+"""), " ")                    // collapse whitespace
        .trim()
}

fun String.stripLeadingArticle(): String {
    return this.trimStart()
        .replace(Regex("""^(the|a|an)\s+"""), "")
        .trim()
}
