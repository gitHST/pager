package unit.data

import Privacy
import com.luke.pager.data.repo.IReviewRepository
import com.luke.pager.data.viewmodel.ReviewViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import unit.MainDispatcherRule

class ReviewRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var reviewRepository: IReviewRepository
    private lateinit var reviewViewModel: ReviewViewModel

    @Before
    fun setUp() {
        reviewRepository = mockk()
        reviewViewModel = ReviewViewModel(reviewRepository)
    }

    @Test
    fun `deleteReviewAndBookById should delegate to repository`() = runTest {
        val reviewId = "review-1"

        coEvery { reviewRepository.deleteReviewAndBookById(reviewId) } returns Unit

        reviewViewModel.deleteReviewAndBookById(reviewId)

        coVerify { reviewRepository.deleteReviewAndBookById(reviewId) }
    }

    @Test
    fun `updateReviewText should delegate to repository`() = runTest {
        val reviewId = "review-1"
        val newText = "Updated review text"

        coEvery { reviewRepository.updateReviewText(reviewId, newText) } returns Unit

        reviewViewModel.updateReviewText(reviewId, newText)

        coVerify { reviewRepository.updateReviewText(reviewId, newText) }
    }

    @Test
    fun `updateReviewRating should delegate to repository`() = runTest {
        val reviewId = "review-1"
        val newRating = 4.5f

        coEvery { reviewRepository.updateReviewRating(reviewId, newRating) } returns Unit

        reviewViewModel.updateReviewRating(reviewId, newRating)

        coVerify { reviewRepository.updateReviewRating(reviewId, newRating) }
    }

    @Test
    fun `updateReviewPrivacy should delegate to repository`() = runTest {
        val reviewId = "review-1"
        val newPrivacy = Privacy.PUBLIC

        coEvery { reviewRepository.updateReviewPrivacy(reviewId, newPrivacy) } returns Unit

        reviewViewModel.updateReviewPrivacy(reviewId, newPrivacy)

        coVerify { reviewRepository.updateReviewPrivacy(reviewId, newPrivacy) }
    }
}
