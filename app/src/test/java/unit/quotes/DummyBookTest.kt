package unit.quotes

import com.luke.pager.data.entities.BookEntity
import com.luke.pager.screens.quotescreen.quotelist.DummyBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DummyBookTest {

    @Test
    fun `toBookEntity maps id and title and sets cover null`() {
        val dummy = DummyBook(
            id = -1L,
            title = "Dummy Book 1"
        )

        val entity: BookEntity = dummy.toBookEntity()

        assertEquals(-1L, entity.id)
        assertEquals("Dummy Book 1", entity.title)
        assertNull(entity.cover)
    }
}
