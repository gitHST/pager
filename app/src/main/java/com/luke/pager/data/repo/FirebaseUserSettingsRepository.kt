package com.luke.pager.data.repo

import Privacy
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
                    Log.w("UserSettingsRepo", "Theme listener error", error)
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

    override val defaultPrivacyFlow: Flow<Privacy> =
        callbackFlow {
            val listener = settingsDocument.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("UserSettingsRepo", "Privacy listener error", error)
                    trySend(Privacy.PUBLIC)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val privacyString = snapshot.getString("default_privacy")
                    val privacy = privacyString?.toPrivacyOrNull() ?: Privacy.PUBLIC
                    trySend(privacy)
                } else {
                    trySend(Privacy.PUBLIC)
                }
            }

            awaitClose { listener.remove() }
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        try {
            settingsDocument
                .set(
                    mapOf("theme_mode" to mode.name),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.w("UserSettingsRepo", "Failed to set theme mode", e)
        }
    }

    override suspend fun setDefaultPrivacy(privacy: Privacy) {
        try {
            settingsDocument
                .set(
                    mapOf("default_privacy" to privacy.name),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            Log.w("UserSettingsRepo", "Failed to set default privacy", e)
        }
    }
}

private fun String.toThemeModeOrNull(): ThemeMode? =
    try {
        ThemeMode.valueOf(this)
    } catch (_: IllegalArgumentException) {
        null
    }

private fun String.toPrivacyOrNull(): Privacy? =
    try {
        Privacy.valueOf(this)
    } catch (_: IllegalArgumentException) {
        null
    }
