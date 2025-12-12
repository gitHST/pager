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
    firestore: FirebaseFirestore = Firebase.firestore,
) : IUserSettingsRepository {

    private val settingsDocument: DocumentReference =
        firestore.collection("users")
            .document(uid)
            .collection("settings")
            .document("app")

    override val themeModeFlow: Flow<Result<ThemeMode>> =
        callbackFlow {
            val listener = settingsDocument.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("UserSettingsRepo", "Theme listener error", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val mode =
                    if (snapshot != null && snapshot.exists()) {
                        val modeString = snapshot.getString("theme_mode")
                        modeString?.toThemeModeOrNull() ?: ThemeMode.LIGHT
                    } else {
                        ThemeMode.LIGHT
                    }

                trySend(Result.success(mode))
            }

            awaitClose { listener.remove() }
        }

    override val defaultPrivacyFlow: Flow<Result<Privacy>> =
        callbackFlow {
            val listener = settingsDocument.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("UserSettingsRepo", "Privacy listener error", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val privacy =
                    if (snapshot != null && snapshot.exists()) {
                        val privacyString = snapshot.getString("default_privacy")
                        privacyString?.toPrivacyOrNull() ?: Privacy.PUBLIC
                    } else {
                        Privacy.PUBLIC
                    }

                trySend(Result.success(privacy))
            }

            awaitClose { listener.remove() }
        }

    override val syncOverCellularFlow: Flow<Result<Boolean>> =
        callbackFlow {
            val listener = settingsDocument.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("UserSettingsRepo", "Cellular sync listener error", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val value =
                    if (snapshot != null && snapshot.exists()) {
                        snapshot.getBoolean("sync_over_cellular") ?: true
                    } else {
                        true
                    }

                trySend(Result.success(value))
            }

            awaitClose { listener.remove() }
        }

    override suspend fun setThemeMode(mode: ThemeMode): Result<Unit> {
        return try {
            settingsDocument
                .set(
                    mapOf("theme_mode" to mode.name),
                    SetOptions.merge(),
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("UserSettingsRepo", "Failed to set theme mode", e)
            Result.failure(e)
        }
    }

    override suspend fun setDefaultPrivacy(privacy: Privacy): Result<Unit> {
        return try {
            settingsDocument
                .set(
                    mapOf("default_privacy" to privacy.name),
                    SetOptions.merge(),
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("UserSettingsRepo", "Failed to set default privacy", e)
            Result.failure(e)
        }
    }

    override suspend fun setSyncOverCellular(enabled: Boolean): Result<Unit> {
        return try {
            settingsDocument
                .set(
                    mapOf("sync_over_cellular" to enabled),
                    SetOptions.merge(),
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("UserSettingsRepo", "Failed to set sync over cellular", e)
            Result.failure(e)
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
