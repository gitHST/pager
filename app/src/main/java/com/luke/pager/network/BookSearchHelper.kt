package com.luke.pager.network

suspend fun searchBooksSmart(rawQuery: String): List<OpenLibraryBook> {
    val query = rawQuery.trim()
    val lowerQuery = query.lowercase()

    val titleResponse = OpenLibraryService.api.searchBooks(title = query)
    val authorResponse = OpenLibraryService.api.searchBooks(author = query)

    val combined = (titleResponse.docs + authorResponse.docs)

    val seenTitles = mutableSetOf<String>()

    val scored = combined.mapNotNull { book ->
        val normTitle = book.title.normalizeTitle()
        if (normTitle in seenTitles) return@mapNotNull null
        seenTitles += normTitle

        val titleLower = book.title.lowercase()
        val authorMatch = book.author_name?.any { it.equals(query, ignoreCase = true) } == true
        val titleMatch = titleLower.contains(lowerQuery)

        // Title length penalty: more extra words = lower score
        val lengthPenalty = (book.title.length - query.length).coerceAtLeast(0)

        val finalScore = when {
            authorMatch -> 300
            titleMatch -> 200 - (lengthPenalty / 3)
            else -> 100
        }


        finalScore to book
    }

    return scored
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

