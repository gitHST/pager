package com.luke.pager.data.repo

import Privacy
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.ReviewEntity
import kotlinx.coroutines.tasks.await

class FirebaseReviewRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : IReviewRepository {

    private val reviewsCollection = firestore.collection("reviews")
    private val booksCollection = firestore.collection("books")
    private val quotesCollection = firestore.collection("quotes")

    override suspend fun insertReview(review: ReviewEntity) {
        val idLong =
            if (review.id == 0) {
                System.currentTimeMillis()
            } else {
                review.id.toLong()
            }

        val reviewForFirestore = review.copy(id = idLong.toInt())
        val docId = idLong.toString()

        reviewsCollection
            .document(docId)
            .set(reviewForFirestore.toFirestoreMap())
            .await()
    }

    override suspend fun getAllReviews(): List<ReviewEntity> {
        val snapshot = reviewsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toReviewEntityOrNull() }
    }

    override suspend fun deleteReviewAndBookById(reviewId: Long) {
        val reviewDocId = reviewId.toString()

        val reviewSnapshot = reviewsCollection.document(reviewDocId).get().await()
        val bookId = reviewSnapshot.getLong("book_id")

        reviewsCollection.document(reviewDocId).delete().await()

        if (bookId != null) {
            val bookDocId = bookId.toString()

            booksCollection.document(bookDocId).delete().await()

            val quotesSnapshot =
                quotesCollection.whereEqualTo("book_id", bookId).get().await()
            for (doc in quotesSnapshot.documents) {
                doc.reference.delete().await()
            }
        }
    }

    override suspend fun updateReviewText(
        reviewId: Long,
        newText: String
    ) {
        reviewsCollection
            .document(reviewId.toString())
            .update("review_text", newText)
            .await()
    }

    override suspend fun updateReviewRating(
        reviewId: Long,
        newRating: Float
    ) {
        reviewsCollection
            .document(reviewId.toString())
            .update("rating", newRating)
            .await()
    }

    override suspend fun updateReviewPrivacy(
        reviewId: Long,
        privacy: Privacy
    ) {
        reviewsCollection
            .document(reviewId.toString())
            .update("privacy", privacy.name)
            .await()
    }


    private fun ReviewEntity.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "id" to id.toLong(),
            "book_id" to bookId,
            "date_started_reading" to dateStartedReading,
            "date_finished_reading" to dateFinishedReading,
            "date_reviewed" to dateReviewed,
            "rating" to rating,
            "review_text" to reviewText,
            "tags" to tags,
            "privacy" to privacy.name,
            "has_spoilers" to hasSpoilers
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toReviewEntityOrNull(): ReviewEntity? {
        val idLong = getLong("id") ?: runCatching { id.toLong() }.getOrNull()
        val bookId = getLong("book_id") ?: return null

        val dateStartedReading = getString("date_started_reading")
        val dateFinishedReading = getString("date_finished_reading")
        val dateReviewed = getString("date_reviewed")
        val rating = getDouble("rating")?.toFloat()
        val reviewText = getString("review_text")
        val tags = getString("tags")

        val privacyName = getString("privacy")
        val privacy =
            privacyName
                ?.let { runCatching { Privacy.valueOf(it) }.getOrDefault(Privacy.PUBLIC) }
                ?: Privacy.PUBLIC

        val hasSpoilers = getBoolean("has_spoilers") ?: false

        return ReviewEntity(
            id = (idLong ?: 0L).toInt(),
            bookId = bookId,
            dateStartedReading = dateStartedReading,
            dateFinishedReading = dateFinishedReading,
            dateReviewed = dateReviewed,
            rating = rating,
            reviewText = reviewText,
            tags = tags,
            privacy = privacy,
            hasSpoilers = hasSpoilers
        )
    }
}
