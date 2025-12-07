package unit.data

import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.IBookRepository
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.data.viewmodel.BookViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import unit.MainDispatcherRule

class BookRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var bookRepository: IBookRepository
    private lateinit var reviewRepository: IReviewRepository
    private lateinit var bookViewModel: BookViewModel

    @Before
    fun setUp() {
        bookRepository = mockk()
        reviewRepository = mockk()
        bookViewModel = BookViewModel(bookRepository, reviewRepository)
    }

    @Test
    fun `insertAndReturnId should delegate to repository and return id`() = runTest {
        val book = BookEntity(
            id = "",
            title = "Test Book"
        )
        val generatedId = "generated-book-id"

        coEvery { bookRepository.insertAndReturnId(book) } returns generatedId

        val result = bookViewModel.insertAndReturnId(book)

        assertEquals(generatedId, result)
        coVerify { bookRepository.insertAndReturnId(book) }
    }

    @Test
    fun `loadBooks should collect flow from repository into books state`() = runTest {
        val books = listOf(
            BookEntity(
                id = "1",
                title = "Book 1"
            )
        )

        every { bookRepository.getAllBooks() } returns flowOf(books)

        bookViewModel.loadBooks()

        // UnconfinedTestDispatcher runs launches immediately
        assertEquals(books, bookViewModel.books.value)
    }

    @Test
    fun `loadAllReviews should populate allReviews map keyed by bookId`() = runTest {
        val reviews = listOf(
            ReviewEntity(
                id = "r1",
                bookId = "b1"
            ),
            ReviewEntity(
                id = "r2",
                bookId = "b2"
            )
        )

        coEvery { reviewRepository.getAllReviews() } returns reviews

        bookViewModel.loadAllReviews()

        val expected = reviews.associateBy { it.bookId }
        assertEquals(expected, bookViewModel.allReviews.value)
    }
}
