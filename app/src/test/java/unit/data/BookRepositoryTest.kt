package unit.data

import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.repo.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BookRepositoryTest {

    private lateinit var bookDao: BookDao
    private lateinit var bookRepository: BookRepository

    @Before
    fun setUp() {
        bookDao = mockk()
        bookRepository = BookRepository(bookDao)
    }

    @Test
    fun `insertAndReturnId should call DAO method`() = runTest {
        val book = BookEntity(title = "Test Book")
        coEvery { bookDao.insertAndReturnId(book) } returns 1L

        val result = bookRepository.insertAndReturnId(book)

        assertEquals(1L, result)
        coVerify { bookDao.insertAndReturnId(book) }
    }

    @Test
    fun `getAllBooks should return flow from DAO`() = runTest {
        val books = listOf(BookEntity(title = "Book1"))
        coEvery { bookDao.getAllBooks() } returns flowOf(books)

        val flow = bookRepository.getAllBooks()
        flow.collect { result ->
            assertEquals(books, result)
        }
        coVerify { bookDao.getAllBooks() }
    }
}
