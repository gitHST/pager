package unit.network

import com.luke.pager.network.normalizeTitle
import com.luke.pager.network.stripLeadingArticle
import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {
    @Test
    fun `normalizeTitle removes quotes and subtitles`() {
        val input = "\"The Great Adventure: A Journey (Special Edition)\""
        val expected = "the great adventure"
        assertEquals(expected, input.normalizeTitle())
    }

    @Test
    fun `stripLeadingArticle removes leading articles`() {
        assertEquals("Great Adventure", "The Great Adventure".stripLeadingArticle())
        assertEquals("Adventure", "An Adventure".stripLeadingArticle())
        assertEquals("Journey", "A Journey".stripLeadingArticle())
    }
}
