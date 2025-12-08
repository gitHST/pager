package unit.network

import com.luke.pager.network.OpenLibraryService
import org.junit.Assert.assertNotNull
import org.junit.Test

class OpenLibraryServiceTest {
    @Test
    fun `OpenLibraryService api is initialized`() {
        val api = OpenLibraryService.api
        assertNotNull(api)
    }
}
