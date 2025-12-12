package com.luke.pager.data.repo

import Privacy
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.ReviewEntity
import kotlinx.coroutines.tasks.await

class FirebaseReviewRepository(
    private val uid: String,
    firestore: FirebaseFirestore = Firebase.firestore,
) : IReviewRepository {

    private val reviewsCollection =
        firestore
            .collection("users")
            .document(uid)
            .collection("reviews")

    private val booksCollection =
        firestore
            .collection("users")
            .document(uid)
            .collection("books")

    private val quotesCollection =
        firestore
            .collection("users")
            .document(uid)
            .collection("quotes")

    private val globalBooksCollection = firestore.collection("books")

    override suspend fun insertReview(review: ReviewEntity): Result<Unit> {
        return try {
            val docRef = reviewsCollection.document()
            val id = docRef.id

            val reviewToSave = review.copy(id = id)
            docRef.set(reviewToSave.toFirestoreMap()).await()

            reviewToSave.bookKey?.let { bookKey ->
                val safeBookId = bookKey.toFirestoreSafeId()
                val globalReviewRef =
                    globalBooksCollection
                        .document(safeBookId)
                        .collection("reviews")
                        .document(id)

                try {
                    globalReviewRef.set(reviewToSave.toGlobalReviewMap(uid)).await()
                } catch (e: Exception) {
                    Log.w("FirebaseReviewRepo", "Global review mirror write failed (ignored)", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseReviewRepo", "insertReview failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllReviews(): Result<List<ReviewEntity>> {
        return try {
            val snapshot = reviewsCollection.get().await()
            Result.success(snapshot.documents.mapNotNull { it.toReviewEntityOrNull() })
        } catch (e: Exception) {
            Log.w("FirebaseReviewRepo", "getAllReviews failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReviewAndBookById(reviewId: String): Result<Unit> {
        return try {
            val reviewDoc = reviewsCollection.document(reviewId).get().await()
            val bookId = reviewDoc.getString("book_id") ?: return Result.success(Unit)
            val bookKey = reviewDoc.getString("book_key")

            reviewsCollection.document(reviewId).delete().await()

            if (bookKey != null) {
                val safeBookId = bookKey.toFirestoreSafeId()
                try {
                    globalBooksCollection
                        .document(safeBookId)
                        .collection("reviews")
                        .document(reviewId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    Log.w("FirebaseReviewRepo", "Global review mirror delete failed (ignored)", e)
                }
            }

            val remainingReviewsSnapshot =
                reviewsCollection
                    .whereEqualTo("book_id", bookId)
                    .get()
                    .await()

            if (remainingReviewsSnapshot.isEmpty) {
                booksCollection.document(bookId).delete().await()

                val quoteSnapshot =
                    quotesCollection
                        .whereEqualTo("book_id", bookId)
                        .get()
                        .await()

                for (q in quoteSnapshot.documents) {
                    q.reference.delete().await()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseReviewRepo", "deleteReviewAndBookById failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReviewText(
        reviewId: String,
        newText: String,
    ): Result<Unit> {
        return try {
            reviewsCollection
                .document(reviewId)
                .update("review_text", newText)
                .await()

            val reviewDoc = reviewsCollection.document(reviewId).get().await()
            val bookKey = reviewDoc.getString("book_key")
            if (bookKey != null) {
                val safeBookId = bookKey.toFirestoreSafeId()
                try {
                    globalBooksCollection
                        .document(safeBookId)
                        .collection("reviews")
                        .document(reviewId)
                        .update("review_text", newText)
                        .await()
                } catch (e: Exception) {
                    Log.w("FirebaseReviewRepo", "Global review mirror update failed (ignored)", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseReviewRepo", "updateReviewText failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReviewRating(
        reviewId: String,
        newRating: Float,
    ): Result<Unit> {
        return try {
            reviewsCollection
                .document(reviewId)
                .update("rating", newRating)
                .await()

            val reviewDoc = reviewsCollection.document(reviewId).get().await()
            val bookKey = reviewDoc.getString("book_key")
            if (bookKey != null) {
                val safeBookId = bookKey.toFirestoreSafeId()
                try {
                    globalBooksCollection
                        .document(safeBookId)
                        .collection("reviews")
                        .document(reviewId)
                        .update("rating", newRating)
                        .await()
                } catch (e: Exception) {
                    Log.w("FirebaseReviewRepo", "Global review mirror update failed (ignored)", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseReviewRepo", "updateReviewRating failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateReviewPrivacy(
        reviewId: String,
        privacy: Privacy,
    ): Result<Unit> {
        return try {
            reviewsCollection
                .document(reviewId)
                .update("privacy", privacy.name)
                .await()

            val reviewDoc = reviewsCollection.document(reviewId).get().await()
            val bookKey = reviewDoc.getString("book_key")
            if (bookKey != null) {
                val safeBookId = bookKey.toFirestoreSafeId()
                try {
                    globalBooksCollection
                        .document(safeBookId)
                        .collection("reviews")
                        .document(reviewId)
                        .update("privacy", privacy.name)
                        .await()
                } catch (e: Exception) {
                    Log.w("FirebaseReviewRepo", "Global review mirror update failed (ignored)", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseReviewRepo", "updateReviewPrivacy failed", e)
            Result.failure(e)
        }
    }

    private fun ReviewEntity.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "book_id" to bookId,
            "book_key" to bookKey,
            "date_started_reading" to dateStartedReading,
            "date_finished_reading" to dateFinishedReading,
            "date_reviewed" to dateReviewed,
            "rating" to rating,
            "review_text" to reviewText,
            "tags" to tags,
            "privacy" to privacy.name,
            "has_spoilers" to hasSpoilers,
        )

    private fun ReviewEntity.toGlobalReviewMap(userId: String): Map<String, Any?> =
        mapOf(
            "uid" to userId,
            "book_id" to bookId,
            "book_key" to bookKey,
            "date_started_reading" to dateStartedReading,
            "date_finished_reading" to dateFinishedReading,
            "date_reviewed" to dateReviewed,
            "rating" to rating,
            "review_text" to reviewText,
            "tags" to tags,
            "privacy" to privacy.name,
            "has_spoilers" to hasSpoilers,
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toReviewEntityOrNull(): ReviewEntity? {
        val id = this.id
        val bookId = getString("book_id") ?: return null
        val bookKey = getString("book_key")
        val dateStartedReading = getString("date_started_reading")
        val dateFinishedReading = getString("date_finished_reading")
        val dateReviewed = getString("date_reviewed")
        val rating = getDouble("rating")?.toFloat()
        val reviewText = getString("review_text")
        val tags = getString("tags")
        val privacyName = getString("privacy")
        val hasSpoilers = getBoolean("has_spoilers") ?: false

        val privacy =
            runCatching { Privacy.valueOf(privacyName ?: "") }
                .getOrDefault(Privacy.PUBLIC)

        return ReviewEntity(
            id = id,
            bookId = bookId,
            bookKey = bookKey,
            dateStartedReading = dateStartedReading,
            dateFinishedReading = dateFinishedReading,
            dateReviewed = dateReviewed,
            rating = rating,
            reviewText = reviewText,
            tags = tags,
            privacy = privacy,
            hasSpoilers = hasSpoilers,
        )
    }
}

private fun String.toFirestoreSafeId(): String =
    this.trimStart('/').replace('/', '_')
