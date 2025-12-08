package com.luke.pager.data.repo

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

    override suspend fun getQuotesByBookId(bookId: String): List<QuoteEntity> {
        val snapshot =
            quotesCollection.whereEqualTo("book_id", bookId).get().await()

        return snapshot.documents.mapNotNull { it.toQuoteEntityOrNull() }
    }

    override suspend fun insertQuote(quote: QuoteEntity) {
        val docRef = quotesCollection.document()
        val id = docRef.id

        val quoteToSave = quote.copy(id = id)

        docRef.set(quoteToSave.toFirestoreMap()).await()
    }

    override suspend fun getAllQuotes(): List<QuoteEntity> {
        val snapshot =
            quotesCollection.orderBy("date_added").get().await()

        return snapshot.documents.mapNotNull { it.toQuoteEntityOrNull() }
    }

    override suspend fun updateQuote(quote: QuoteEntity) {
        if (quote.id.isBlank()) {
            insertQuote(quote)
            return
        }

        quotesCollection
            .document(quote.id)
            .set(quote.toFirestoreMap())
            .await()
    }

    override suspend fun deleteQuote(quote: QuoteEntity) {
        if (quote.id.isBlank()) return

        quotesCollection
            .document(quote.id)
            .delete()
            .await()
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
