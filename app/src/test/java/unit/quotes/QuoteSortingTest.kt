package unit.quotes

import com.luke.pager.data.entities.QuoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class QuoteSortingTest {

    private fun sortQuotesByDateAdded(
        quotes: List<QuoteEntity>,
        ascending: Boolean
    ): List<QuoteEntity> {
        return if (ascending) {
            quotes.sortedBy { it.dateAdded }
        } else {
            quotes.sortedByDescending { it.dateAdded }
        }
    }

    @Test
    fun `sortQuotesByDateAdded sorts ascending by date string`() {
        val q1 = QuoteEntity(
            id = 1L,
            bookId = 1L,
            quoteText = "Oldest",
            pageNumber = null,
            dateAdded = "2025-01-01 10:00:00"
        )
        val q2 = QuoteEntity(
            id = 2L,
            bookId = 1L,
            quoteText = "Middle",
            pageNumber = null,
            dateAdded = "2025-01-02 09:00:00"
        )
        val q3 = QuoteEntity(
            id = 3L,
            bookId = 1L,
            quoteText = "Newest",
            pageNumber = null,
            dateAdded = "2025-01-03 08:00:00"
        )

        val unsorted = listOf(q2, q3, q1)

        val sorted = sortQuotesByDateAdded(unsorted, ascending = true)

        assertEquals(listOf(q1, q2, q3).map { it.id }, sorted.map { it.id })
    }

    @Test
    fun `sortQuotesByDateAdded sorts descending by date string`() {
        val q1 = QuoteEntity(
            id = 1L,
            bookId = 1L,
            quoteText = "Oldest",
            pageNumber = null,
            dateAdded = "2025-01-01 10:00:00"
        )
        val q2 = QuoteEntity(
            id = 2L,
            bookId = 1L,
            quoteText = "Middle",
            pageNumber = null,
            dateAdded = "2025-01-02 09:00:00"
        )
        val q3 = QuoteEntity(
            id = 3L,
            bookId = 1L,
            quoteText = "Newest",
            pageNumber = null,
            dateAdded = "2025-01-03 08:00:00"
        )

        val unsorted = listOf(q2, q1, q3)

        val sorted = sortQuotesByDateAdded(unsorted, ascending = false)

        assertEquals(listOf(q3, q2, q1).map { it.id }, sorted.map { it.id })
    }
}
