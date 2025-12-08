package unit.screens

import com.luke.pager.screens.getDateWithoutTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateUtilsTest {
    @Test
    fun `returns formatted month and year if date is older`() {
        val inputDate = "2023-04-25 10:00:00"
        val expectedFormat =
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(inputDate)!!,
            )
        assertEquals(expectedFormat, getDateWithoutTime(inputDate))
    }

    @Test
    fun `returns month only if date is within current year`() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val dateString = "$currentYear-03-10 10:00:00"
        val expectedMonth = "March"
        assertEquals(expectedMonth, getDateWithoutTime(dateString))
    }

    @Test
    fun `returns Unknown Date for malformed date`() {
        assertEquals("Unknown Date", getDateWithoutTime("invalid date"))
    }

    @Test
    fun `returns Unknown Date for null`() {
        assertEquals("Unknown Date", getDateWithoutTime(null))
    }
}
