package com.luke.pager.network

suspend fun searchBooksSmart(rawQuery: String): List<OpenLibraryBook> {
    val query = rawQuery.trim()
    val lowerQuery = query.lowercase()

    val titleResponse = OpenLibraryService.api.searchBooks(title = query)
    val authorResponse = OpenLibraryService.api.searchBooks(author = query)

    val combined = (titleResponse.docs + authorResponse.docs)

    val seen = mutableMapOf<Pair<String, String>, OpenLibraryBook>() // (normalizedTitleNoArticle, author) -> bestBook

    val result = mutableListOf<Pair<Int, OpenLibraryBook>>()

    for (book in combined) {
        val normTitle = book.title.normalizeTitle()
        val strippedTitle = normTitle.stripLeadingArticle()
        val author = book.author_name?.firstOrNull()?.lowercase() ?: continue
        val key = strippedTitle to author

        val existing = seen[key]
        val keepThis = existing == null || normTitle.length > existing.title.normalizeTitle().length

        if (keepThis) {
            seen[key] = book
        }
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

    return result
        .sortedWith(compareByDescending<Pair<Int, OpenLibraryBook>> { it.first }
            .thenBy { it.second.title.lowercase() })
        .map { it.second }
}




    fun String.normalizeTitle(): String {
    return this.lowercase()
        .replace(Regex("""^["'“”‘’]+|["'“”‘’]+$"""), "") // remove leading/trailing quotes
        .replace(Regex("""\s*\(.*?\)"""), "")              // Remove parentheses and content
        .replace(Regex(""":.*$"""), "")                    // Remove subtitles after colon
        .replace(Regex("""-.*$"""), "")                    // Remove subtitles after dash
        .replace(Regex("""\s+"""), " ")                    // Collapse whitespace
        .trim()
}

fun String.stripLeadingArticle(): String {
    return this.trimStart()
        .replace(Regex("""^(the|a|an)\s+"""), "")
        .trim()
}
