package com.luke.pager.data.repo

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.QuoteEntity
import kotlinx.coroutines.tasks.await

class FirebaseQuoteRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : IQuoteRepository {

    private val quotesCollection = firestore.collection("quotes")

    override suspend fun getQuotesByBookId(bookId: Long): List<QuoteEntity> {
        val snapshot =
            quotesCollection
                .whereEqualTo("book_id", bookId)
                .get()
                .await()

        return snapshot.documents.mapNotNull { it.toQuoteEntityOrNull() }
    }

    override suspend fun insertQuote(quote: QuoteEntity) {
        val id =
            if (quote.id == 0L) {
                System.currentTimeMillis()
            } else {
                quote.id
            }

        val quoteForFirestore = quote.copy(id = id)

        quotesCollection
            .document(id.toString())
            .set(quoteForFirestore.toFirestoreMap())
            .await()
    }

    override suspend fun getAllQuotes(): List<QuoteEntity> {
        val snapshot =
            quotesCollection
                .orderBy("date_added")
                .get()
                .await()

        return snapshot.documents.mapNotNull { it.toQuoteEntityOrNull() }
    }

    override suspend fun updateQuote(quote: QuoteEntity) {
        val id =
            if (quote.id == 0L) {
                insertQuote(quote)
                return
            } else {
                quote.id
            }

        val quoteForFirestore = quote.copy(id = id)

        quotesCollection
            .document(id.toString())
            .set(quoteForFirestore.toFirestoreMap())
            .await()
    }

    override suspend fun deleteQuote(quote: QuoteEntity) {
        val id = quote.id
        if (id == 0L) return

        quotesCollection
            .document(id.toString())
            .delete()
            .await()
    }

    private fun QuoteEntity.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "book_id" to bookId,
            "quote_text" to quoteText,
            "page_number" to pageNumber,
            "date_added" to dateAdded
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toQuoteEntityOrNull(): QuoteEntity? {
        val idLong = getLong("id") ?: runCatching { id.toLong() }.getOrNull()
        val bookId = getLong("book_id") ?: return null
        val quoteText = getString("quote_text") ?: return null
        val pageNumber = getLong("page_number")?.toInt()
        val dateAdded = getString("date_added")

        return QuoteEntity(
            id = idLong ?: 0L,
            bookId = bookId,
            quoteText = quoteText,
            pageNumber = pageNumber,
            dateAdded = dateAdded
        )
    }
}