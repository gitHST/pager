package unit.data

import com.luke.pager.data.dao.QuoteDao
import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.repo.QuoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class QuoteRepositoryTest {

    private lateinit var quoteDao: QuoteDao
    private lateinit var quoteRepository: QuoteRepository

    @Before
    fun setUp() {
        quoteDao = mockk()
        quoteRepository = QuoteRepository(quoteDao)
    }

    @Test
    fun `getQuotesByBookId should call DAO and return quotes`() = runTest {
        val bookId = 1L
        val quotes = listOf(
            QuoteEntity(
                id = 1L,
                bookId = bookId,
                quoteText = "Test quote",
                pageNumber = 42,
                dateAdded = "2025-01-01 12:00:00"
            )
        )

        coEvery { quoteDao.getQuotesByBookId(bookId) } returns quotes

        val result = quoteRepository.getQuotesByBookId(bookId)

        assertEquals(quotes, result)
        coVerify { quoteDao.getQuotesByBookId(bookId) }
    }

    @Test
    fun `insertQuote should delegate to DAO`() = runTest {
        val quote = QuoteEntity(
            id = 0L,
            bookId = 1L,
            quoteText = "New quote",
            pageNumber = null,
            dateAdded = "2025-01-01 12:00:00"
        )

        coEvery { quoteDao.insertQuote(quote) } returns Unit

        quoteRepository.insertQuote(quote)

        coVerify { quoteDao.insertQuote(quote) }
    }

    @Test
    fun `getAllQuotes should return list from DAO`() = runTest {
        val quotes = listOf(
            QuoteEntity(
                id = 1L,
                bookId = 1L,
                quoteText = "Quote 1",
                pageNumber = 10,
                dateAdded = "2025-01-01 10:00:00"
            ),
            QuoteEntity(
                id = 2L,
                bookId = 2L,
                quoteText = "Quote 2",
                pageNumber = 20,
                dateAdded = "2025-01-02 11:00:00"
            )
        )

        coEvery { quoteDao.getAllQuotes() } returns quotes

        val result = quoteRepository.getAllQuotes()

        assertEquals(quotes, result)
        coVerify { quoteDao.getAllQuotes() }
    }

    @Test
    fun `updateQuote should delegate to DAO`() = runTest {
        val quote = QuoteEntity(
            id = 1L,
            bookId = 1L,
            quoteText = "Updated quote",
            pageNumber = 12,
            dateAdded = "2025-01-01 12:00:00"
        )

        coEvery { quoteDao.updateQuote(quote) } returns Unit

        quoteRepository.updateQuote(quote)

        coVerify { quoteDao.updateQuote(quote) }
    }

    @Test
    fun `deleteQuote should delegate to DAO`() = runTest {
        val quote = QuoteEntity(
            id = 1L,
            bookId = 1L,
            quoteText = "To delete",
            pageNumber = 99,
            dateAdded = "2025-01-01 12:00:00"
        )

        coEvery { quoteDao.deleteQuote(quote) } returns Unit

        quoteRepository.deleteQuote(quote)

        coVerify { quoteDao.deleteQuote(quote) }
    }
}
