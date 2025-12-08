package unit.quotes

import com.luke.pager.data.entities.QuoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteSortingTest {
    private fun sortQuotesByDateAdded(
        quotes: List<QuoteEntity>,
        ascending: Boolean,
    ): List<QuoteEntity> =
        if (ascending) {
            quotes.sortedBy { it.dateAdded }
        } else {
            quotes.sortedByDescending { it.dateAdded }
        }

    @Test
    fun `sortQuotesByDateAdded sorts ascending by date string`() {
        val q1 =
            QuoteEntity(
                id = "1",
                bookId = "book-1",
                quoteText = "Oldest",
                pageNumber = null,
                dateAdded = "2025-01-01 10:00:00",
            )
        val q2 =
            QuoteEntity(
                id = "2",
                bookId = "book-1",
                quoteText = "Middle",
                pageNumber = null,
                dateAdded = "2025-01-02 09:00:00",
            )
        val q3 =
            QuoteEntity(
                id = "3",
                bookId = "book-1",
                quoteText = "Newest",
                pageNumber = null,
                dateAdded = "2025-01-03 08:00:00",
            )

        val unsorted = listOf(q2, q3, q1)

        val sorted = sortQuotesByDateAdded(unsorted, ascending = true)

        assertEquals(listOf("1", "2", "3"), sorted.map { it.id })
    }

    @Test
    fun `sortQuotesByDateAdded sorts descending by date string`() {
        val q1 =
            QuoteEntity(
                id = "1",
                bookId = "book-1",
                quoteText = "Oldest",
                pageNumber = null,
                dateAdded = "2025-01-01 10:00:00",
            )
        val q2 =
            QuoteEntity(
                id = "2",
                bookId = "book-1",
                quoteText = "Middle",
                pageNumber = null,
                dateAdded = "2025-01-02 09:00:00",
            )
        val q3 =
            QuoteEntity(
                id = "3",
                bookId = "book-1",
                quoteText = "Newest",
                pageNumber = null,
                dateAdded = "2025-01-03 08:00:00",
            )

        val unsorted = listOf(q2, q1, q3)

        val sorted = sortQuotesByDateAdded(unsorted, ascending = false)

        assertEquals(listOf("3", "2", "1"), sorted.map { it.id })
    }
}
