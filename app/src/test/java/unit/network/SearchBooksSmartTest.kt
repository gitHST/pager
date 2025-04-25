package unit.network

import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.network.OpenLibrarySearchResponse
import com.luke.pager.network.OpenLibraryService
import com.luke.pager.network.searchBooksSmart
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SearchBooksSmartTest {

    @Before
    fun setUp() {
        mockkObject(OpenLibraryService)
    }

    @Test
    fun `searchBooksSmart returns combined deduplicated sorted books`() = runTest {
        val titleBooks = listOf(
            OpenLibraryBook("1", "The Great Adventure", listOf("Alice"), 101, 2000),
            OpenLibraryBook("2", "Great Adventure", listOf("Alice"), 102, 2001)
        )
        val authorBooks = listOf(
            OpenLibraryBook("3", "Adventure Time", listOf("Alice"), 103, 2002)
        )

        coEvery { OpenLibraryService.api.searchBooks(title = "Adventure") } returns OpenLibrarySearchResponse(titleBooks)
        coEvery { OpenLibraryService.api.searchBooks(author = "Adventure") } returns OpenLibrarySearchResponse(authorBooks)

        val result = searchBooksSmart("Adventure")

        assertNull(result.errorMessage)
        assertEquals(2, result.books.size)
        assertTrue(result.books.any { it.title == "Great Adventure" })
        assertTrue(result.books.any { it.title == "Adventure Time" })
    }

    @Test
    fun `searchBooksSmart handles HttpException 503`() = runTest {
        coEvery { OpenLibraryService.api.searchBooks(any(), any()) } throws HttpException(Response.error<Any>(503,
            "".toResponseBody(null)
        ))

        val result = searchBooksSmart("Adventure")

        assertEquals("Server is currently unavailable.", result.errorMessage)
        assertTrue(result.books.isEmpty())
    }

    @Test
    fun `searchBooksSmart handles IOException`() = runTest {
        coEvery { OpenLibraryService.api.searchBooks(any(), any()) } throws IOException()

        val result = searchBooksSmart("Adventure")

        assertEquals("Network error. Please check your connection.", result.errorMessage)
        assertTrue(result.books.isEmpty())
    }
}
