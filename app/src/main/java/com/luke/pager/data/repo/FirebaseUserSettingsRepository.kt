package com.luke.pager.data.repo

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.luke.pager.ui.theme.ThemeMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserSettingsRepository(
    uid: String,
    firestore: FirebaseFirestore = Firebase.firestore
) : IUserSettingsRepository {

    private val settingsDocument: DocumentReference =
        firestore.collection("users")
            .document(uid)
            .collection("settings")
            .document("app")

    override val themeModeFlow: Flow<ThemeMode> =
        callbackFlow {
            val listener = settingsDocument.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ThemeMode.SYSTEM)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val modeString = snapshot.getString("theme_mode")
                    val mode = modeString?.toThemeModeOrNull() ?: ThemeMode.SYSTEM
                    trySend(mode)
                } else {
                    trySend(ThemeMode.SYSTEM)
                }
            }

            awaitClose { listener.remove() }
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsDocument.set(
            mapOf(
                "theme_mode" to mode.name
            )
        ).await()
    }
}

private fun String.toThemeModeOrNull(): ThemeMode? =
    try {
        ThemeMode.valueOf(this)
    } catch (_: IllegalArgumentException) {
        null
    }
