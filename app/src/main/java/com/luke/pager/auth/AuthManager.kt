package com.luke.pager.auth

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

object AuthManager {
    suspend fun ensureAnonymousUser(): String {
        val auth = Firebase.auth

        auth.currentUser?.let { return it.uid }

        auth.signInAnonymously().await()

        return auth.currentUser!!.uid
    }

    val uid: String
        get() = Firebase.auth.currentUser!!.uid
}
