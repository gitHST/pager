package com.luke.pager.data.sample

import com.luke.pager.data.AppDatabase
import com.luke.pager.data.entities.BookEntity
import com.luke.pager.data.entities.ReviewEntity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(DelicateCoroutinesApi::class)
fun seedDatabaseIfEmpty(db: AppDatabase) {
    val bookDao = db.bookDao()
    val reviewDao = db.reviewDao()

    GlobalScope.launch {
        bookDao.getAllBooks().collect { books ->
            if (books.isEmpty()) {

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val calendar = Calendar.getInstance()

                val nineteenEightyFourDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("1984-06-08") ?: Date()
                )

                val currentDate = sdf.format(calendar.time)
                calendar.time = Date()
                calendar.add(Calendar.MONTH, -3)
                val threeMonthsAgo = sdf.format(calendar.time)

                calendar.time = Date()
                calendar.add(Calendar.MONTH, -6)
                val sixMonthsAgo = sdf.format(calendar.time)

                calendar.time = Date()
                calendar.add(Calendar.YEAR, -1)
                val oneYearAgo = sdf.format(calendar.time)

                val id1 = bookDao.insertAndReturnId(
                    BookEntity(
                        title = "1984",
                        authors = "George Orwell"
                    )
                )

                val id2 = bookDao.insertAndReturnId(
                    BookEntity(
                        title = "Peppa pig",
                        authors = "Wolfgang Amadeus Mozart"
                    )
                )

                val id3 = bookDao.insertAndReturnId(
                    BookEntity(
                        title = "Brave New World",
                        authors = "Aldous Huxley"
                    )
                )

                val id6 = bookDao.insertAndReturnId(
                    BookEntity(
                        title = "The Hitchhiker's Guide to the Galaxy",
                        authors = "Douglas Adams"
                    )
                )

                val id7 = bookDao.insertAndReturnId(
                    BookEntity(
                        title = "The Great Gatsby",
                        authors = "F. Scott Fitzgerald"
                    )
                )

                reviewDao.insertReview(
                    ReviewEntity(
                        bookId = id1,
                        dateReviewed = nineteenEightyFourDate,
                        reviewText = "Bleak, dystopian, an absolute nightmare to be honest with you. Those are just my interviewing techniques...",
                    )
                )

                reviewDao.insertReview(
                    ReviewEntity(
                        bookId = id2,
                        dateReviewed = currentDate,
                        reviewText = "daddy pig is my favourite"
                    )
                )

                reviewDao.insertReview(
                    ReviewEntity(
                        bookId = id3,
                        dateReviewed = oneYearAgo,
                        reviewText = "I haven't read this so dk what to put here for sample data... I just haven't read much"
                    )
                )

                reviewDao.insertReview(
                    ReviewEntity(
                        bookId = id6,
                        dateReviewed = sixMonthsAgo,
                        reviewText = "Something about the number 42 idk"
                    )
                )

                reviewDao.insertReview(
                    ReviewEntity(
                        bookId = id7,
                        dateReviewed = threeMonthsAgo,
                        reviewText = "When the gatsby is great 😋😋😋😋😋😋"
                    )
                )
            }
        }
    }
}