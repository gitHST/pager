package unit.data

import com.luke.pager.data.entities.QuoteEntity
import com.luke.pager.data.repo.IQuoteRepository
import com.luke.pager.data.viewmodel.QuoteViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import unit.MainDispatcherRule

class QuoteRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var quoteRepository: IQuoteRepository
    private lateinit var quoteViewModel: QuoteViewModel

    @Before
    fun setUp() {
        quoteRepository = mockk()
        quoteViewModel = QuoteViewModel(quoteRepository)
    }

    @Test
    fun `loadQuotesForBook should call repository and update quotes state`() =
        runTest {
            val bookId = "book-1"
            val quotes =
                listOf(
                    QuoteEntity(
                        id = "q1",
                        bookId = bookId,
                        quoteText = "Test quote",
                        pageNumber = 42,
                        dateAdded = "2025-01-01 12:00:00",
                    ),
                )

            coEvery { quoteRepository.getQuotesByBookId(bookId) } returns quotes

            quoteViewModel.loadQuotesForBook(bookId)

            assertEquals(quotes, quoteViewModel.quotes.value)
            coVerify { quoteRepository.getQuotesByBookId(bookId) }
        }

    @Test
    fun `loadAllQuotes should call repository and update allQuotes state`() =
        runTest {
            val quotes =
                listOf(
                    QuoteEntity(
                        id = "q1",
                        bookId = "book-1",
                        quoteText = "Quote 1",
                        pageNumber = 10,
                        dateAdded = "2025-01-01 10:00:00",
                    ),
                    QuoteEntity(
                        id = "q2",
                        bookId = "book-2",
                        quoteText = "Quote 2",
                        pageNumber = 20,
                        dateAdded = "2025-01-02 11:00:00",
                    ),
                )

            coEvery { quoteRepository.getAllQuotes() } returns quotes

            quoteViewModel.loadAllQuotes()

            assertEquals(quotes, quoteViewModel.allQuotes.value)
            coVerify { quoteRepository.getAllQuotes() }
        }

    @Test
    fun `addQuote should insert quote and refresh both quotes and allQuotes`() =
        runTest {
            val bookId = "book-1"
            val newQuote =
                QuoteEntity(
                    id = "",
                    bookId = bookId,
                    quoteText = "New quote",
                    pageNumber = null,
                    dateAdded = "2025-01-03 09:00:00",
                )

            val quotesForBook =
                listOf(
                    newQuote.copy(id = "q-new"),
                )
            val allQuotes = quotesForBook

            coEvery { quoteRepository.insertQuote(newQuote) } returns Unit
            coEvery { quoteRepository.getQuotesByBookId(bookId) } returns quotesForBook
            coEvery { quoteRepository.getAllQuotes() } returns allQuotes

            quoteViewModel.addQuote(newQuote)

            assertEquals(quotesForBook, quoteViewModel.quotes.value)
            assertEquals(allQuotes, quoteViewModel.allQuotes.value)

            coVerify { quoteRepository.insertQuote(newQuote) }
            coVerify { quoteRepository.getQuotesByBookId(bookId) }
            coVerify { quoteRepository.getAllQuotes() }
        }

    @Test
    fun `updateQuote should delegate to repository and refresh quotes`() =
        runTest {
            val bookId = "book-1"
            val quote =
                QuoteEntity(
                    id = "q1",
                    bookId = bookId,
                    quoteText = "Updated quote",
                    pageNumber = 12,
                    dateAdded = "2025-01-01 12:00:00",
                )

            val quotesForBook = listOf(quote)
            val allQuotes = quotesForBook

            coEvery { quoteRepository.updateQuote(quote) } returns Unit
            coEvery { quoteRepository.getQuotesByBookId(bookId) } returns quotesForBook
            coEvery { quoteRepository.getAllQuotes() } returns allQuotes

            quoteViewModel.updateQuote(quote)

            assertEquals(quotesForBook, quoteViewModel.quotes.value)
            assertEquals(allQuotes, quoteViewModel.allQuotes.value)

            coVerify { quoteRepository.updateQuote(quote) }
            coVerify { quoteRepository.getQuotesByBookId(bookId) }
            coVerify { quoteRepository.getAllQuotes() }
        }

    @Test
    fun `deleteQuote should delegate to repository and refresh quotes`() =
        runTest {
            val bookId = "book-1"
            val quote =
                QuoteEntity(
                    id = "q1",
                    bookId = bookId,
                    quoteText = "To delete",
                    pageNumber = 99,
                    dateAdded = "2025-01-01 12:00:00",
                )

            val quotesForBookAfter = emptyList<QuoteEntity>()
            val allQuotesAfter = emptyList<QuoteEntity>()

            coEvery { quoteRepository.deleteQuote(quote) } returns Unit
            coEvery { quoteRepository.getQuotesByBookId(bookId) } returns quotesForBookAfter
            coEvery { quoteRepository.getAllQuotes() } returns allQuotesAfter

            quoteViewModel.deleteQuote(quote)

            assertEquals(quotesForBookAfter, quoteViewModel.quotes.value)
            assertEquals(allQuotesAfter, quoteViewModel.allQuotes.value)

            coVerify { quoteRepository.deleteQuote(quote) }
            coVerify { quoteRepository.getQuotesByBookId(bookId) }
            coVerify { quoteRepository.getAllQuotes() }
        }
}
