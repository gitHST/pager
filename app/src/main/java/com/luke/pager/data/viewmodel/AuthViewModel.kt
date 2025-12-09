package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val firebaseAuth = Firebase.auth

    private val _isLoggedIn =
        MutableStateFlow(firebaseAuth.currentUser?.isAnonymous == false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> get() = _authError

    fun clearError() {
        _authError.value = null
    }

    /**
     * Login with email/password.
     *
     * NOTE: If the user is currently anonymous and logs in to an *existing* account,
     * this will switch to that account (different uid). Any data stored under the
     * anonymous uid won't be visible under the new uid. For that first-time
     * account creation flow, use [register] which upgrades the anon user instead.
     */
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _authError.value = null

                firebaseAuth
                    .signInWithEmailAndPassword(email.trim(), password)
                    .await()

                _isLoggedIn.value = firebaseAuth.currentUser?.isAnonymous == false
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Login failed"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    /**
     * Register a new email/password account.
     *
     * If the current user is anonymous, we *link* the email credential to the
     * existing anonymous user. This keeps the same uid, so all Firestore data
     * under users/{uid} (books, reviews, quotes, settings) is preserved.
     */
    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _authError.value = null

                val currentUser = firebaseAuth.currentUser
                val credential =
                    EmailAuthProvider.getCredential(email.trim(), password)

                if (currentUser != null && currentUser.isAnonymous) {
                    currentUser.linkWithCredential(credential).await()
                } else {
                    firebaseAuth
                        .createUserWithEmailAndPassword(email.trim(), password)
                        .await()
                }

                _isLoggedIn.value = firebaseAuth.currentUser?.isAnonymous == false
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Registration failed"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _authError.value = null
    }
}
