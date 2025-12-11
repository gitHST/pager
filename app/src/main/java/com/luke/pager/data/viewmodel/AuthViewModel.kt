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
import androidx.core.content.edit
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
import java.io.File

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
        context: Context,
        newName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            val msg = "Not logged in"
            _authError.value = msg
            onError(msg)
            return
        }

        viewModelScope.launch {
            try {
                _authError.value = null
                val trimmed = newName.trim()

                setPendingDisplayName(context, trimmed)
                markDisplayNamePending(context, true)

                onSuccess()

                try {
                    uploadDisplayNameToFirebase(trimmed)
                } catch (e: Exception) {
                    val msg = e.message ?: "Name will sync when you're online"
                    _authError.value = msg
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to update profile"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    fun getInitialDisplayName(context: Context): String {
        val user = firebaseAuth.currentUser
        return if (isDisplayNamePending(context)) {
            getPendingDisplayName(context) ?: user?.displayName.orEmpty()
        } else {
            user?.displayName.orEmpty()
        }
    }

    fun tryUploadPendingDisplayName(
        context: Context,
        onComplete: (Boolean) -> Unit = {},
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            if (!isDisplayNamePending(context)) {
                onComplete(true)
                return@launch
            }

            val pendingName = getPendingDisplayName(context)
            if (pendingName.isNullOrBlank()) {
                markDisplayNamePending(context, false)
                onComplete(true)
                return@launch
            }

            try {
                uploadDisplayNameToFirebase(pendingName)
                onComplete(true)
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to sync display name"
                _authError.value = msg
                onComplete(false)
            }
        }
    }

    private suspend fun uploadDisplayNameToFirebase(
        name: String,
    ) {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No current user")

        val profileUpdates =
            userProfileChangeRequest {
                displayName = name.ifBlank { null }
            }

        user.updateProfile(profileUpdates).await()

        _isLoggedIn.value = user.isAnonymous == false

        markDisplayNamePending(user = null, context = null, pending = false)
    }

    private fun markDisplayNamePending(
        user: Any? = null,
        context: Context? = null,
        pending: Boolean,
    ) {
        if (context != null) {
            val prefs =
                context.getSharedPreferences("profile_name_prefs", Context.MODE_PRIVATE)
            prefs.edit {
                putBoolean("display_name_pending", pending)
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _authError.value = null
    }

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

                val bytes = cropAndCompressProfileImage(
                    context = context,
                    imageUri = imageUri,
                    zoom = zoom,
                    containerSize = containerSize,
                    offsetPx = offsetPx,
                )

                val localUriString = withContext(Dispatchers.IO) {
                    val file = profilePhotoFile(context, uid)
                    file.outputStream().use { it.write(bytes) }

                    markProfilePhotoPending(context, true)

                    Uri.fromFile(file).toString()
                }

                _isLoggedIn.value = user.isAnonymous == false
                onSuccess(localUriString)

                try {
                    uploadProfilePhotoToFirebase(
                        context = context,
                        uid = uid,
                        bytes = bytes,
                    )
                } catch (e: Exception) {
                    val msg = e.message ?: "Profile photo will sync when you're online"
                    _authError.value = msg
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to update profile photo"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    fun tryUploadPendingProfilePhoto(
        context: Context,
        onComplete: (Boolean) -> Unit = {},
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val uid = user.uid
            val pending = isProfilePhotoPending(context)
            val file = profilePhotoFile(context, uid)

            if (!pending || !file.exists()) {
                markProfilePhotoPending(context, false)
                onComplete(true)
                return@launch
            }

            try {
                val bytes = withContext(Dispatchers.IO) {
                    file.readBytes()
                }

                uploadProfilePhotoToFirebase(
                    context = context,
                    uid = uid,
                    bytes = bytes,
                )

                onComplete(true)
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to sync profile photo"
                _authError.value = msg
                onComplete(false)
            }
        }
    }

    private suspend fun uploadProfilePhotoToFirebase(
        context: Context,
        uid: String,
        bytes: ByteArray,
    ) {
        val firestore = Firebase.firestore

        val settingsDoc = try {
            firestore
                .collection("users")
                .document(uid)
                .collection("settings")
                .document("app")
                .get()
                .await()
        } catch (e: Exception) {
            throw e
        }

        val allowCellular =
            settingsDoc.getBoolean("sync_over_cellular") ?: false

        if (!canSyncNow(context, allowCellular)) {
            throw IllegalStateException("Waiting for allowed network to upload profile photo")
        }

        val storageRef =
            FirebaseStorage
                .getInstance()
                .reference
                .child("users/$uid/profile.jpg")

        storageRef.putBytes(bytes).await()
        val downloadUri = storageRef.downloadUrl.await()

        val user = firebaseAuth.currentUser ?: return
        val profileUpdates = userProfileChangeRequest {
            photoUri = downloadUri
        }
        user.updateProfile(profileUpdates).await()

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

        markProfilePhotoPending(context, false)
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


    fun getOfflineFirstDisplayName(context: Context): String {
        return if (isDisplayNamePending(context)) {
            getPendingDisplayName(context) ?: firebaseAuth.currentUser?.displayName.orEmpty()
        } else {
            firebaseAuth.currentUser?.displayName.orEmpty()
        }
    }

    fun getOfflineFirstProfilePhotoUri(context: Context): Uri? {
        val user = firebaseAuth.currentUser ?: return null
        val uid = user.uid

        val cachedFile = profilePhotoFile(context, uid)
        return if (cachedFile.exists()) {
            Uri.fromFile(cachedFile)
        } else {
            user.photoUrl
        }
    }
    private fun profilePhotoFile(context: Context, uid: String): File =
        File(context.filesDir, "profile_photo_${uid}.jpg")

    private fun markProfilePhotoPending(context: Context, pending: Boolean) {
        val prefs =
            context.getSharedPreferences("profile_photo_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("profile_photo_pending", pending)
        }
    }

    private fun isProfilePhotoPending(context: Context): Boolean {
        val prefs =
            context.getSharedPreferences("profile_photo_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("profile_photo_pending", false)
    }


    private fun setPendingDisplayName(context: Context, name: String) {
        val prefs =
            context.getSharedPreferences("profile_name_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("pending_display_name", name)
        }
    }

    private fun getPendingDisplayName(context: Context): String? {
        val prefs =
            context.getSharedPreferences("profile_name_prefs", Context.MODE_PRIVATE)
        return prefs.getString("pending_display_name", null)
    }

    private fun markDisplayNamePending(context: Context, pending: Boolean) {
        val prefs =
            context.getSharedPreferences("profile_name_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("display_name_pending", pending)
        }
    }

    private fun isDisplayNamePending(context: Context): Boolean {
        val prefs =
            context.getSharedPreferences("profile_name_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("display_name_pending", false)
    }
}
