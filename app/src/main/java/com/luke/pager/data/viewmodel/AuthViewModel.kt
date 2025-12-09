package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
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

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
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

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
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

    fun updateDisplayName(
        newName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onError("Not logged in")
            return
        }

        viewModelScope.launch {
            try {
                _authError.value = null

                val profileUpdates =
                    userProfileChangeRequest {
                        displayName =
                            newName
                                .trim()
                                .ifBlank { null }
                    }

                user.updateProfile(profileUpdates).await()

                _isLoggedIn.value = user.isAnonymous == false
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to update profile"
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
