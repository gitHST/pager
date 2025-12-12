package com.luke.pager.data.repo

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.data.entities.BookEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseBookRepository(
    uid: String,
    firestore: FirebaseFirestore = Firebase.firestore,
) : IBookRepository {
    private val booksCollection =
        firestore
            .collection("users")
            .document(uid)
            .collection("books")

    override fun getAllBooks(): Flow<Result<List<BookEntity>>> =
        callbackFlow {
            val listener =
                booksCollection.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseBookRepo", "Books listener error", error)
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }

                    val books =
                        snapshot?.documents?.mapNotNull { doc ->
                            doc.toBookEntityOrNull()
                        } ?: emptyList()

                    trySend(Result.success(books)).isSuccess
                }

            awaitClose { listener.remove() }
        }

    override suspend fun insertAndReturnId(book: BookEntity): Result<String> =
        try {
            val docRef = booksCollection.document()
            val id = docRef.id

            val bookToSave = book.copy(id = id)
            docRef.set(bookToSave.toFirestoreMap()).await()

            Result.success(id)
        } catch (e: Exception) {
            Log.w("FirebaseBookRepo", "insertAndReturnId failed", e)
            Result.failure(e)
        }

    private fun com.google.firebase.firestore.DocumentSnapshot.toBookEntityOrNull(): BookEntity? {
        val id = this.id

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
        val coverId = getLong("cover_id")?.toInt()

        return BookEntity(
            id = id,
            title = title,
            authors = authors,
            isbn = isbn,
            cover = null,
            coverId = coverId,
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
            dateAdded = dateAdded,
        )
    }

    private fun BookEntity.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "title" to title,
            "authors" to authors,
            "isbn" to isbn,
            "cover_id" to coverId,
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
            "date_added" to dateAdded,
        )
}
