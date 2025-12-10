package com.luke.pager.data.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.luke.pager.network.canSyncNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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

    /**
     * Crop + compress the image to match the circular preview,
     * upload to Firebase Storage, update Auth profile photoUrl,
     * and store the URL in Firestore.
     */
    fun updateProfilePhoto(
        context: Context,
        imageUri: Uri,
        zoom: Float,
        containerSize: IntSize,
        offsetPx: Offset,
        onSuccess: (String) -> Unit = {},
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
                val uid = user.uid

                // 1) Read sync_over_cellular from Firestore (default = false)
                val firestore = Firebase.firestore
                val settingsDoc =
                    firestore
                        .collection("users")
                        .document(uid)
                        .collection("settings")
                        .document("app")
                        .get()
                        .await()

                val allowCellular =
                    settingsDoc.getBoolean("sync_over_cellular") ?: false

                // 2) Check current network type vs setting
                if (!canSyncNow(context, allowCellular)) {
                    val msg = "Waiting for Wi-Fi to upload profile photo"
                    _authError.value = msg
                    onError(msg)
                    return@launch
                }

                // 3) Crop + compress locally
                val bytes = cropAndCompressProfileImage(
                    context = context,
                    imageUri = imageUri,
                    zoom = zoom,
                    containerSize = containerSize,
                    offsetPx = offsetPx,
                )

                // 4) Upload to Firebase Storage
                val storageRef =
                    FirebaseStorage
                        .getInstance()
                        .reference
                        .child("users/$uid/profile.jpg")

                storageRef.putBytes(bytes).await()
                val downloadUri = storageRef.downloadUrl.await()

                // 5) Update Auth profile
                val profileUpdates = userProfileChangeRequest {
                    photoUri = downloadUri
                }
                user.updateProfile(profileUpdates).await()

                // 6) Store URL in Firestore settings
                firestore
                    .collection("users")
                    .document(uid)
                    .collection("settings")
                    .document("app")
                    .set(
                        mapOf("profile_photo_url" to downloadUri.toString()),
                        SetOptions.merge(),
                    )
                    .await()

                _isLoggedIn.value = user.isAnonymous == false
                onSuccess(downloadUri.toString())
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to update profile photo"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    private suspend fun cropAndCompressProfileImage(
        context: Context,
        imageUri: Uri,
        zoom: Float,
        containerSize: IntSize,
        offsetPx: Offset,
        outSize: Int = 512,
        quality: Int = 80,
    ): ByteArray =
        withContext(Dispatchers.IO) {
            val inputStream =
                context.contentResolver.openInputStream(imageUri)
                    ?: throw IllegalArgumentException("Cannot open image URI")

            inputStream.use { stream ->
                val original =
                    BitmapFactory.decodeStream(stream)
                        ?: throw IllegalArgumentException("Cannot decode image")

                val bw = original.width.toFloat()
                val bh = original.height.toFloat()
                val containerWidth = containerSize.width.toFloat().coerceAtLeast(1f)
                val containerHeight = containerSize.height.toFloat().coerceAtLeast(1f)

                val baseScale = maxOf(containerWidth / bw, containerHeight / bh)

                val matrix = Matrix().apply {
                    postScale(baseScale, baseScale)
                    val dx = (containerWidth - bw * baseScale) / 2f
                    val dy = (containerHeight - bh * baseScale) / 2f
                    postTranslate(dx, dy)

                    val cx = containerWidth / 2f
                    val cy = containerHeight / 2f
                    postTranslate(-cx, -cy)
                    postScale(zoom, zoom)
                    postTranslate(cx, cy)

                    postTranslate(offsetPx.x, offsetPx.y)

                    val destScale = outSize.toFloat() / containerWidth
                    postScale(destScale, destScale)
                }

                val output = createBitmap(outSize, outSize)
                val canvas = Canvas(output)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                canvas.drawBitmap(original, matrix, paint)

                original.recycle()

                val baos = ByteArrayOutputStream()
                output.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), baos)
                output.recycle()
                baos.toByteArray()
            }
        }
}