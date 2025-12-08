package unit.network

import com.luke.pager.network.OpenLibraryApi
import com.luke.pager.network.OpenLibraryBook
import com.luke.pager.network.OpenLibrarySearchResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class OpenLibraryApiTest {
    private val api = mockk<OpenLibraryApi>()

    @Test
    fun `searchBooks returns expected data`() =
        runTest {
            val mockResponse =
                OpenLibrarySearchResponse(
                    docs =
                        listOf(
                            OpenLibraryBook("key1", "Title 1", listOf("Author 1"), 123, 2000),
                            OpenLibraryBook("key2", "Title 2", listOf("Author 2"), null, 2005),
                        ),
                )
            coEvery { api.searchBooks(title = "Test", author = null) } returns mockResponse

            val result = api.searchBooks(title = "Test")

            assertEquals(2, result.docs.size)
            assertEquals("Title 1", result.docs[0].title)
            assertEquals(123, result.docs[0].coverIndex)
        }

    @Test
    fun `searchBooks throws exception`() =
        runTest {
            coEvery { api.searchBooks(title = "Error", author = null) } throws RuntimeException("Network error")

            assertThrows(RuntimeException::class.java) {
                runTest {
                    api.searchBooks(title = "Error")
                }
            }
        }
}
