package com.luke.pager.data.viewmodel

import android.app.Activity
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.luke.pager.R
import com.luke.pager.network.canSyncNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
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

    fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                _authError.value = null

                val serverClientId = activity.getString(R.string.default_web_client_id)

                val credentialManager = CredentialManager.create(activity)

                val googleIdOption =
                    GetGoogleIdOption
                        .Builder()
                        .setServerClientId(serverClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build()

                val request =
                    GetCredentialRequest
                        .Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                val result =
                    try {
                        credentialManager.getCredential(
                            context = activity,
                            request = request,
                        )
                    } catch (e: GetCredentialException) {
                        val msg = "Google sign-in failed: ${e.type}"
                        _authError.value = msg
                        onError(msg)
                        return@launch
                    }

                val googleIdToken = extractGoogleIdToken(result.credential)
                if (googleIdToken.isNullOrBlank()) {
                    val msg = "Google sign-in failed (no ID token)"
                    _authError.value = msg
                    onError(msg)
                    return@launch
                }

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val isNewUser = authResult.additionalUserInfo?.isNewUser == true

                if (isNewUser) {
                    firebaseAuth.currentUser
                        ?.updateProfile(
                            userProfileChangeRequest { photoUri = null },
                        )?.await()
                }

                _isLoggedIn.value = firebaseAuth.currentUser?.isAnonymous == false
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Google sign-in failed"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    private fun extractGoogleIdToken(credential: androidx.credentials.Credential): String? =
        try {
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                null
            }
        } catch (_: GoogleIdTokenParsingException) {
            null
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
                val uid = user.uid

                cacheDisplayName(context, uid, trimmed, pending = true)

                onSuccess()

                try {
                    uploadDisplayNameToFirebase(trimmed)
                    cacheDisplayName(context, uid, trimmed, pending = false)
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

    fun getOfflineFirstDisplayName(context: Context): String {
        val user = firebaseAuth.currentUser ?: return ""
        val uid = user.uid

        val cached = getCachedDisplayName(context, uid)
        return cached ?: user.displayName.orEmpty()
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
            val uid = user.uid
            if (!isDisplayNamePending(context, uid)) {
                onComplete(true)
                return@launch
            }

            val pendingName = getCachedDisplayName(context, uid)
            if (pendingName.isNullOrBlank()) {
                cacheDisplayName(context, uid, null, pending = false)
                onComplete(true)
                return@launch
            }

            try {
                uploadDisplayNameToFirebase(pendingName)
                cacheDisplayName(context, uid, pendingName, pending = false)
                onComplete(true)
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to sync display name"
                _authError.value = msg
                onComplete(false)
            }
        }
    }

    private suspend fun uploadDisplayNameToFirebase(name: String) {
        val user = firebaseAuth.currentUser ?: throw IllegalStateException("No current user")
        val uid = user.uid

        val trimmed = name.trim()

        val profileUpdates =
            userProfileChangeRequest {
                displayName = trimmed.ifBlank { null }
            }
        user.updateProfile(profileUpdates).await()
        _isLoggedIn.value = user.isAnonymous == false

        Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("settings")
            .document("app")
            .set(
                mapOf("display_name" to trimmed),
                SetOptions.merge(),
            ).await()
    }

    fun logout(context: Context) {
        val uid = firebaseAuth.currentUser?.uid

        viewModelScope.coroutineContext.cancelChildren()

        if (uid != null) {
            clearLocalIdentityCache(context, uid)
        }

        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _authError.value = null
    }

    fun deleteAccountAndData(
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
        onReauthRequired: () -> Unit = {},
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

                val firestore = Firebase.firestore
                val storage = FirebaseStorage.getInstance()

                suspend fun deleteSubcollection(path: String) {
                    val colRef = firestore.collection("users").document(uid).collection(path)
                    val snapshot = colRef.get().await()
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                }

                deleteSubcollection("books")
                deleteSubcollection("reviews")
                deleteSubcollection("quotes")
                deleteSubcollection("settings")

                firestore
                    .collection("users")
                    .document(uid)
                    .delete()
                    .await()

                val userStorageRef = storage.reference.child("users/$uid")
                try {
                    val listResult = userStorageRef.listAll().await()
                    for (item in listResult.items) item.delete().await()
                    for (prefix in listResult.prefixes) {
                        val subList = prefix.listAll().await()
                        for (subItem in subList.items) subItem.delete().await()
                    }
                } catch (_: Exception) {
                }

                clearLocalIdentityCache(context, uid)

                try {
                    user.delete().await()
                } catch (e: FirebaseAuthRecentLoginRequiredException) {
                    onReauthRequired()
                    return@launch
                } catch (e: Exception) {
                    val msg =
                        e.message ?: "Failed to delete account. Re-authentication may be required."
                    _authError.value = msg
                    onError(msg)
                    return@launch
                }

                _isLoggedIn.value = false
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to delete account and data"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    data class RemoteProfileUpdate(
        val displayName: String?,
        val profilePhotoUri: String?,
    )

    suspend fun refreshProfileFromFirestore(context: Context): Result<RemoteProfileUpdate> {
        val user =
            firebaseAuth.currentUser
                ?: return Result.failure(IllegalStateException("No user logged in"))

        val uid = user.uid

        return runCatching {
            val doc =
                Firebase.firestore
                    .collection("users")
                    .document(uid)
                    .collection("settings")
                    .document("app")
                    .get()
                    .await()

            val remoteName = doc.getString("display_name")?.trim()
            val remotePhotoUrl = doc.getString("profile_photo_url")?.trim()

            if (!remoteName.isNullOrBlank()) {
                cacheDisplayName(context, uid, remoteName, pending = false)
            }

            if (!remotePhotoUrl.isNullOrBlank()) {
                runCatching {
                    val bytes =
                        withContext(Dispatchers.IO) {
                            java.net
                                .URL(remotePhotoUrl)
                                .openStream()
                                .use { it.readBytes() }
                        }
                    withContext(Dispatchers.IO) {
                        val file = profilePhotoFile(context, uid)
                        file.outputStream().use { it.write(bytes) }
                    }
                    setProfilePhotoPending(context, uid, false)
                }
            }

            val localPhotoUri = getOfflineFirstProfilePhotoUri(context)?.toString()

            RemoteProfileUpdate(
                displayName = remoteName,
                profilePhotoUri = localPhotoUri,
            )
        }
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

                val bytes =
                    cropAndCompressProfileImage(
                        context = context,
                        imageUri = imageUri,
                        zoom = zoom,
                        containerSize = containerSize,
                        offsetPx = offsetPx,
                    )

                val localUriString =
                    withContext(Dispatchers.IO) {
                        val file = profilePhotoFile(context, uid)
                        file.outputStream().use { it.write(bytes) }
                        setProfilePhotoPending(context, uid, true)
                        Uri.fromFile(file).toString()
                    }

                _isLoggedIn.value = true
                onSuccess(localUriString)

                try {
                    uploadProfilePhotoToFirebase(
                        context = context,
                        uid = uid,
                        bytes = bytes,
                    )
                } catch (_: Exception) {
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to update profile photo"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    fun getOfflineFirstProfilePhotoUri(context: Context): Uri? {
        val user = firebaseAuth.currentUser ?: return null
        val uid = user.uid
        val file = profilePhotoFile(context, uid)
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            user.photoUrl
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
            if (!isProfilePhotoPending(context, uid)) {
                onComplete(true)
                return@launch
            }

            val file = profilePhotoFile(context, uid)
            if (!file.exists()) {
                setProfilePhotoPending(context, uid, false)
                onComplete(true)
                return@launch
            }

            try {
                val bytes =
                    withContext(Dispatchers.IO) {
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
        val profileUpdates =
            userProfileChangeRequest {
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
            ).await()

        setProfilePhotoPending(context, uid, false)
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

                val matrix =
                    Matrix().apply {
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

    private fun profilePhotoFile(
        context: Context,
        uid: String,
    ): File = File(context.filesDir, "profile_photo_$uid.jpg")

    private fun profilePhotoPrefs(context: Context) = context.getSharedPreferences("profile_photo_prefs", Context.MODE_PRIVATE)

    private fun setProfilePhotoPending(
        context: Context,
        uid: String,
        pending: Boolean,
    ) {
        profilePhotoPrefs(context).edit {
            putBoolean("profile_photo_pending_$uid", pending)
        }
    }

    private fun isProfilePhotoPending(
        context: Context,
        uid: String,
    ): Boolean =
        profilePhotoPrefs(context)
            .getBoolean("profile_photo_pending_$uid", false)

    private fun namePrefs(context: Context) = context.getSharedPreferences("profile_name_prefs", Context.MODE_PRIVATE)

    private fun cacheDisplayName(
        context: Context,
        uid: String,
        name: String?,
        pending: Boolean,
    ) {
        namePrefs(context).edit {
            if (name == null) {
                remove("display_name_$uid")
            } else {
                putString("display_name_$uid", name)
            }
            putBoolean("display_name_pending_$uid", pending)
        }
    }

    private fun getCachedDisplayName(
        context: Context,
        uid: String,
    ): String? = namePrefs(context).getString("display_name_$uid", null)

    private fun isDisplayNamePending(
        context: Context,
        uid: String,
    ): Boolean = namePrefs(context).getBoolean("display_name_pending_$uid", false)

    private fun clearLocalIdentityCache(
        context: Context,
        uid: String,
    ) {
        val file = profilePhotoFile(context, uid)
        if (file.exists()) {
            file.delete()
        }

        namePrefs(context).edit {
            remove("display_name_$uid")
            remove("display_name_pending_$uid")
        }

        profilePhotoPrefs(context).edit {
            remove("profile_photo_pending_$uid")
        }
    }

    fun reauthenticateWithPassword(
        email: String,
        password: String,
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
                val credential = EmailAuthProvider.getCredential(email.trim(), password)
                user.reauthenticate(credential).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Re-authentication failed")
            }
        }
    }

    fun reauthenticateWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                _authError.value = null

                val serverClientId = activity.getString(R.string.default_web_client_id)
                val credentialManager = CredentialManager.create(activity)

                val googleIdOption =
                    GetGoogleIdOption
                        .Builder()
                        .setServerClientId(serverClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build()

                val request =
                    GetCredentialRequest
                        .Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                val result =
                    try {
                        credentialManager.getCredential(
                            context = activity,
                            request = request,
                        )
                    } catch (e: GetCredentialException) {
                        val msg = "Google re-auth failed: ${e.type}"
                        _authError.value = msg
                        onError(msg)
                        return@launch
                    }

                val googleIdToken = extractGoogleIdToken(result.credential)
                if (googleIdToken.isNullOrBlank()) {
                    val msg = "Google re-auth failed (no ID token)"
                    _authError.value = msg
                    onError(msg)
                    return@launch
                }

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                firebaseAuth.currentUser?.reauthenticate(firebaseCredential)?.await()

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Google re-authentication failed")
            }
        }
    }
}
