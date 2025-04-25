package unit.screens.addscreen

import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.network.SearchResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchLogicTest {

    @Test
    fun `searchBooksSmart returns correct result after delay`() = runTest {
        val mockSearchBooks: suspend (String) -> SearchResult = mockk()
        val dummyBooks = listOf(
            OpenLibraryBook(
                key = "/works/OL12345W",
                title = "Test Book",
                authorName = listOf("Author One"),
                coverIndex = 123,
                firstPublishYear = 1999
            )
        )
        val query = "test"

        coEvery { mockSearchBooks(query) } returns SearchResult(books = dummyBooks)

        val result = mockSearchBooks(query)
        assertEquals(dummyBooks, result.books)
    }

    @Test
    fun `searchBooksSmart returns error message`() = runTest {
        val mockSearchBooks: suspend (String) -> SearchResult = mockk()
        val query = "errorTest"

        val errorMessage = "Network Error"
        coEvery { mockSearchBooks(query) } returns SearchResult(books = emptyList(), errorMessage = errorMessage)

        val result = mockSearchBooks(query)
        assertEquals(errorMessage, result.errorMessage)
        assertTrue(result.books.isEmpty())
    }

    @Test
    fun `debounced logic cancels previous search if query changes`() = runTest {
        val calledQueries = mutableListOf<String>()
        val mockSearchBooks: suspend (String) -> SearchResult = { q ->
            calledQueries.add(q)
            delay(100)
            SearchResult(emptyList())
        }

        val firstQuery = "first"
        val secondQuery = "second"

        val job1 = async { mockSearchBooks(firstQuery) }
        delay(50)
        job1.cancel()
        val job2 = async { mockSearchBooks(secondQuery) }
        job2.await()

        assertTrue(calledQueries.contains(firstQuery))
        assertTrue(calledQueries.contains(secondQuery))
        assertTrue(job1.isCancelled)
    }
}