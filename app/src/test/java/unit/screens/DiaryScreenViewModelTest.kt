package unit.screens

import com.luke.pager.data.viewmodel.BookViewModel
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DiaryScreenViewModelTest {

    private lateinit var bookViewModel: BookViewModel

    @Before
    fun setup() {
        bookViewModel = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `calls loadBooks and loadAllReviews on DiaryScreen init`() = runTest {
        bookViewModel.loadBooks()
        bookViewModel.loadAllReviews()

        verify { bookViewModel.loadBooks() }
        verify { bookViewModel.loadAllReviews() }
    }
}
