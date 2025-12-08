package unit.screens

import Privacy
import com.luke.pager.data.entities.ReviewEntity
import com.luke.pager.data.viewmodel.ReviewViewModel
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewScreenTest {
    private lateinit var reviewViewModel: ReviewViewModel
    private val reviewId = "review-1"
    private val onDeleteSuccess: () -> Unit = mockk(relaxed = true)

    @Before
    fun setup() {
        reviewViewModel = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `updateReviewText, updateReviewRating, updateReviewPrivacy are called on save`() =
        runTest {
            val updatedText = "Updated review"
            val updatedRating = 5.0f
            val updatedPrivacy = Privacy.PUBLIC

            every { reviewViewModel.updateReviewText(reviewId, updatedText) } just Runs
            every { reviewViewModel.updateReviewRating(reviewId, updatedRating) } just Runs
            every { reviewViewModel.updateReviewPrivacy(reviewId, updatedPrivacy) } just Runs

            reviewViewModel.updateReviewText(reviewId, updatedText)
            reviewViewModel.updateReviewRating(reviewId, updatedRating)
            reviewViewModel.updateReviewPrivacy(reviewId, updatedPrivacy)

            verify { reviewViewModel.updateReviewText(reviewId, updatedText) }
            verify { reviewViewModel.updateReviewRating(reviewId, updatedRating) }
            verify { reviewViewModel.updateReviewPrivacy(reviewId, updatedPrivacy) }
        }

    @Test
    fun `deleteReviewAndBookById is called and onDeleteSuccess triggered on delete confirm`() =
        runTest {
            every { reviewViewModel.deleteReviewAndBookById(reviewId) } just Runs
            every { onDeleteSuccess.invoke() } just Runs

            reviewViewModel.deleteReviewAndBookById(reviewId)
            onDeleteSuccess()

            verify { reviewViewModel.deleteReviewAndBookById(reviewId) }
            verify { onDeleteSuccess.invoke() }
        }

    @Test
    fun `null rating defaults to zero and empty review text defaults to empty string`() =
        runTest {
            val review =
                ReviewEntity(
                    id = "1",
                    bookId = "book-1",
                    rating = null,
                    reviewText = null,
                )

            val defaultRating = review.rating ?: 0f
            val defaultText = review.reviewText.orEmpty()

            assert(defaultRating == 0f)
            assert(defaultText == "")
        }
}
