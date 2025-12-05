package com.luke.pager.data.repo

import Privacy
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.ReviewEntity
import kotlinx.coroutines.tasks.await

class FirebaseReviewRepository(
    private val uid: String,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : IReviewRepository {

    private val reviewsCollection =
        firestore.collection("users")
            .document(uid)
            .collection("reviews")

    private val booksCollection =
        firestore.collection("users")
            .document(uid)
            .collection("books")

    private val quotesCollection =
        firestore.collection("users")
            .document(uid)
            .collection("quotes")

    override suspend fun insertReview(review: ReviewEntity) {
        val docRef = reviewsCollection.document()
        val id = docRef.id

        val reviewToSave = review.copy(id = id)
        docRef.set(reviewToSave.toFirestoreMap()).await()
    }

    override suspend fun getAllReviews(): List<ReviewEntity> {
        val snapshot = reviewsCollection.get().await()
        return snapshot.documents.mapNotNull { it.toReviewEntityOrNull() }
    }

    override suspend fun deleteReviewAndBookById(reviewId: String) {
        // get review document
        val reviewDoc = reviewsCollection.document(reviewId).get().await()
        val bookId = reviewDoc.getString("book_id") ?: return

        // delete review
        reviewsCollection.document(reviewId).delete().await()

        // delete book
        booksCollection.document(bookId).delete().await()

        // delete associated quotes
        val quoteSnapshot =
            quotesCollection.whereEqualTo("book_id", bookId).get().await()
        for (q in quoteSnapshot.documents) q.reference.delete().await()
    }

    override suspend fun updateReviewText(reviewId: String, newText: String) {
        reviewsCollection.document(reviewId)
            .update("review_text", newText)
            .await()
    }

    override suspend fun updateReviewRating(reviewId: String, newRating: Float) {
        reviewsCollection.document(reviewId)
            .update("rating", newRating)
            .await()
    }

    override suspend fun updateReviewPrivacy(reviewId: String, privacy: Privacy) {
        reviewsCollection.document(reviewId)
            .update("privacy", privacy.name)
            .await()
    }

    // ---------------- Helpers ----------------

    private fun ReviewEntity.toFirestoreMap(): Map<String, Any?> =
        mapOf(
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
        val id = this.id
        val bookId = getString("book_id") ?: return null
        val dateStartedReading = getString("date_started_reading")
        val dateFinishedReading = getString("date_finished_reading")
        val dateReviewed = getString("date_reviewed")
        val rating = getDouble("rating")?.toFloat()
        val reviewText = getString("review_text")
        val tags = getString("tags")
        val privacyName = getString("privacy")
        val hasSpoilers = getBoolean("has_spoilers") ?: false

        val privacy = runCatching { Privacy.valueOf(privacyName ?: "") }
            .getOrDefault(Privacy.PUBLIC)

        return ReviewEntity(
            id = id,
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
