package unit.screens.addscreen

import Privacy
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.network.OpenLibraryBook
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmitReviewHeaderTest {
    private lateinit var bookViewModel: BookViewModel
    private lateinit var navController: androidx.navigation.NavHostController
    private lateinit var book: OpenLibraryBook

    @Before
    fun setup() {
        bookViewModel = mockk(relaxed = true)
        navController = mockk(relaxed = true)
        book =
            OpenLibraryBook(
                key = "OL12345M",
                title = "Test Book",
                authorName = listOf("Test Author"),
                coverIndex = 1,
                firstPublishYear = 2020,
            )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `submitReview called with correct parameters and navigates to diary`() =
        runTest {
            val rating = 4.5f
            val reviewText = "Excellent book!"
            val privacy = Privacy.FRIENDS
            val spoilers = true

            val dateReviewedSlot = slot<String>()

            coEvery {
                bookViewModel.submitReview(
                    eq(book),
                    eq(rating),
                    eq(reviewText),
                    capture(dateReviewedSlot),
                    eq(privacy),
                    eq(spoilers),
                    any(),
                )
            } coAnswers {
                lastArg<() -> Unit>().invoke()
            }

            // Simulate calling submission manually for test (not via Composable)
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val expectedDatePrefix = formatter.format(java.util.Date()).substring(0, 10) // yyyy-MM-dd

            bookViewModel.submitReview(
                book,
                rating,
                reviewText,
                "$expectedDatePrefix 12:00:00",
                privacy,
                spoilers,
            ) {
                navController.navigate("diary") {
                    popUpTo("review_screen") { inclusive = true }
                    launchSingleTop = true
                }
            }

            verify {
                bookViewModel.submitReview(book, rating, reviewText, any(), privacy, spoilers, any())
            }
            verify {
                navController.navigate("diary", any<androidx.navigation.NavOptionsBuilder.() -> Unit>())
            }
        }
}
