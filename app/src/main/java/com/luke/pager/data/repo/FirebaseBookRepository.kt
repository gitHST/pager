package com.luke.pager.data.repo

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.BookEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class FirebaseBookRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : IBookRepository {

    // For now: flat "books" collection.
    // Later you can move to users/{uid}/books when you add Firebase Auth.
    private val booksCollection = firestore.collection("books")

    override fun getAllBooks(): Flow<List<BookEntity>> = callbackFlow {
        val listener = booksCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // You might want to log this or send an error state elsewhere.
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val books = snapshot.documents.mapNotNull { doc ->
                doc.toBookEntityOrNull()
            }

            trySend(books).isSuccess
        }

        awaitClose { listener.remove() }
    }

    override suspend fun insertAndReturnId(book: BookEntity): Long {
        // Simple ID strategy:
        // - if id == 0L, generate one from currentTimeMillis()
        // - otherwise respect existing id
        val id = if (book.id == 0L) {
            System.currentTimeMillis()
        } else {
            book.id
        }

        val docRef = booksCollection.document(id.toString())
        val toSave = book.copy(id = id)

        // We map the entity to a Firestore map (no ByteArray cover).
        docRef.set(toSave.toFirestoreMap()).await()

        return id
    }

    // --- Helpers ---

    private fun com.google.firebase.firestore.DocumentSnapshot.toBookEntityOrNull(): BookEntity? {
        val idFromField = getLong("id")
        val parsedFromDocId = runCatching { this.id.toLong() }.getOrNull()
        val id = idFromField ?: parsedFromDocId ?: return null

        val title = getString("title") ?: return null
        val authors = getString("authors")
        val isbn = getString("isbn")
        val publisher = getString("publisher")
        val publishDate = getString("publish_date")
        val language = getString("language")
        val subject = getString("subject")
        val numberOfPages = getLong("number_of_pages")?.toInt()
        val description = getString("description")
        val edition = getString("edition")
        val openlibraryKey = getString("openlibrary_key")
        val firstPublishDate = getString("first_publish_date")
        val bookmarked = getBoolean("bookmarked") ?: false
        val genres = getString("genres")
        val dateAdded = getString("date_added")

        // NOTE: cover is ByteArray? in Room, but we do NOT store it in Firestore.
        // You’ll typically move that to Firebase Storage and store a URL later.
        return BookEntity(
            id = id,
            title = title,
            authors = authors,
            isbn = isbn,
            cover = null, // no cover bytes from Firestore (yet)
            publisher = publisher,
            publishDate = publishDate,
            language = language,
            subject = subject,
            numberOfPages = numberOfPages,
            description = description,
            edition = edition,
            openlibraryKey = openlibraryKey,
            firstPublishDate = firstPublishDate,
            bookmarked = bookmarked,
            genres = genres,
            dateAdded = dateAdded
        )
    }

    private fun BookEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "authors" to authors,
        "isbn" to isbn,
        "publisher" to publisher,
        "publish_date" to publishDate,
        "language" to language,
        "subject" to subject,
        "number_of_pages" to numberOfPages,
        "description" to description,
        "edition" to edition,
        "openlibrary_key" to openlibraryKey,
        "first_publish_date" to firstPublishDate,
        "bookmarked" to bookmarked,
        "genres" to genres,
        "date_added" to dateAdded
    )
}
