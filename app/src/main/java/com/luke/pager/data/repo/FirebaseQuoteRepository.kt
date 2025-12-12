package com.luke.pager.data.repo

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.QuoteEntity
import kotlinx.coroutines.tasks.await

class FirebaseQuoteRepository(
    uid: String,
    firestore: FirebaseFirestore = Firebase.firestore,
) : IQuoteRepository {
    private val quotesCollection =
        firestore
            .collection("users")
            .document(uid)
            .collection("quotes")

    override suspend fun getQuotesByBookId(bookId: String): Result<List<QuoteEntity>> =
        try {
            val snapshot =
                quotesCollection.whereEqualTo("book_id", bookId).get().await()

            Result.success(snapshot.documents.mapNotNull { it.toQuoteEntityOrNull() })
        } catch (e: Exception) {
            Log.w("FirebaseQuoteRepo", "getQuotesByBookId failed", e)
            Result.failure(e)
        }

    override suspend fun insertQuote(quote: QuoteEntity): Result<Unit> =
        try {
            val docRef = quotesCollection.document()
            val id = docRef.id

            val quoteToSave = quote.copy(id = id)
            docRef.set(quoteToSave.toFirestoreMap()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseQuoteRepo", "insertQuote failed", e)
            Result.failure(e)
        }

    override suspend fun getAllQuotes(): Result<List<QuoteEntity>> =
        try {
            val snapshot =
                quotesCollection.orderBy("date_added").get().await()

            Result.success(snapshot.documents.mapNotNull { it.toQuoteEntityOrNull() })
        } catch (e: Exception) {
            Log.w("FirebaseQuoteRepo", "getAllQuotes failed", e)
            Result.failure(e)
        }

    override suspend fun updateQuote(quote: QuoteEntity): Result<Unit> {
        return try {
            if (quote.id.isBlank()) {
                return insertQuote(quote)
            }

            quotesCollection
                .document(quote.id)
                .set(quote.toFirestoreMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseQuoteRepo", "updateQuote failed", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteQuote(quote: QuoteEntity): Result<Unit> {
        return try {
            if (quote.id.isBlank()) return Result.success(Unit)

            quotesCollection
                .document(quote.id)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirebaseQuoteRepo", "deleteQuote failed", e)
            Result.failure(e)
        }
    }

    private fun QuoteEntity.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "book_id" to bookId,
            "quote_text" to quoteText,
            "page_number" to pageNumber,
            "date_added" to dateAdded,
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toQuoteEntityOrNull(): QuoteEntity? {
        val id = this.id
        val bookId = getString("book_id") ?: return null
        val quoteText = getString("quote_text") ?: return null
        val pageNumber = getLong("page_number")?.toInt()
        val dateAdded = getString("date_added")

        return QuoteEntity(
            id = id,
            bookId = bookId,
            quoteText = quoteText,
            pageNumber = pageNumber,
            dateAdded = dateAdded,
        )
    }
}
