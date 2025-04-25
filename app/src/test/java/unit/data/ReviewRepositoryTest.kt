package unit.data

import com.luke.pager.data.dao.BookDao
import com.luke.pager.data.dao.ReviewDao
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.repo.ReviewRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ReviewRepositoryTest {

    private lateinit var reviewDao: ReviewDao
    private lateinit var bookDao: BookDao
    private lateinit var reviewRepository: ReviewRepository

    @Before
    fun setUp() {
        reviewDao = mockk()
        bookDao = mockk()
        reviewRepository = ReviewRepository(reviewDao, bookDao)
    }

    @Test
    fun `insertReview should call DAO`() = runTest {
        val review = ReviewEntity(bookId = 1L)
        coEvery { reviewDao.insertReview(review) } returns Unit

        reviewRepository.insertReview(review)

        coVerify { reviewDao.insertReview(review) }
    }

    @Test
    fun `deleteReviewAndBookById should delete associated book`() = runTest {
        coEvery { reviewDao.getBookIdByReviewId(1L) } returns 5L
        coEvery { bookDao.deleteBookById(5L) } returns Unit

        reviewRepository.deleteReviewAndBookById(1L)

        coVerify { reviewDao.getBookIdByReviewId(1L) }
        coVerify { bookDao.deleteBookById(5L) }
    }
}
