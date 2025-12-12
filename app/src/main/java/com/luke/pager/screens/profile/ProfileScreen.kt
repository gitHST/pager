package com.luke.pager.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.auth.LoginModal
import com.luke.pager.screens.components.Title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun ProfileScreen(
    navController: NavController,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel,
    authViewModel: AuthViewModel,
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var showLoginModal by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showPfpModal by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firebaseUser = Firebase.auth.currentUser

    val initialProfilePhotoUri: Uri? = remember(firebaseUser?.uid) {
        authViewModel.getOfflineFirstProfilePhotoUri(context)
    }

    var nameInput by remember(firebaseUser?.uid) {
        mutableStateOf(authViewModel.getOfflineFirstDisplayName(context))
    }

    var profilePhotoUri by remember(firebaseUser?.uid) {
        mutableStateOf<Uri?>(initialProfilePhotoUri)
    }
    var profilePhotoZoom by remember(firebaseUser?.uid) {
        mutableFloatStateOf(1f)
    }
    var profilePhotoOffsetFraction by remember(firebaseUser?.uid) {
        mutableStateOf(Offset.Zero)
    }

    var profilePhotoVersion by remember(firebaseUser?.uid) {
        mutableIntStateOf(0)
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri != null) {
                tempPhotoUri = uri
                showPfpModal = true
            }
        }

    val fadedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    val nameToShow = nameInput.ifBlank { "Name" }
    val nameColor =
        if (nameInput.isBlank()) fadedColor
        else MaterialTheme.colorScheme.onBackground

    LaunchedEffect(isLoggedIn, firebaseUser?.uid, isEditing, showPfpModal) {
        val uid = firebaseUser?.uid

        if (!isLoggedIn || uid == null) {
            nameInput = ""
            profilePhotoUri = null
            profilePhotoZoom = 1f
            profilePhotoOffsetFraction = Offset.Zero
            return@LaunchedEffect
        }

        nameInput = authViewModel.getOfflineFirstDisplayName(context)
        profilePhotoUri = authViewModel.getOfflineFirstProfilePhotoUri(context)
        profilePhotoZoom = 1f
        profilePhotoOffsetFraction = Offset.Zero

        if (isEditing || showPfpModal || tempPhotoUri != null) return@LaunchedEffect

        val updated = fetchAndCacheProfilePhotoFromFirestore(
            context = context,
            uid = uid,
        )

        if (updated != null) {
            if (profilePhotoUri?.toString() != updated.toString()) {
                profilePhotoUri = updated
                profilePhotoVersion++
            }
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.tryUploadPendingProfilePhoto(context)
        authViewModel.tryUploadPendingDisplayName(context)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isEditing) {
                IconButton(
                    onClick = {
                        authViewModel.updateDisplayName(
                            context = context,
                            newName = nameInput,
                            onSuccess = { isEditing = false },
                            onError = { _ ->
                                isEditing = false
                            },
                        )
                        showPfpModal = false
                        tempPhotoUri = null
                    },
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp, top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            } else {
                IconButton(
                    onClick = { isEditing = true },
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp, top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit profile",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            IconButton(
                onClick = { navController.navigate("settings") },
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp, top = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Title("Profile")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var avatarSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier =
                    Modifier
                        .size(96.dp)
                        .onSizeChanged { avatarSize = it }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                val model = profilePhotoUri?.let { uri -> "${uri}?v=$profilePhotoVersion" }
                val painter = rememberAsyncImagePainter(model = model)

                val showPlaceholder =
                    profilePhotoUri == null ||
                        painter.state is AsyncImagePainter.State.Empty ||
                        painter.state is AsyncImagePainter.State.Error

                if (!showPlaceholder) {
                    val avatarWidth = avatarSize.width.toFloat().coerceAtLeast(1f)
                    val avatarHeight = avatarSize.height.toFloat().coerceAtLeast(1f)

                    Image(
                        painter = painter,
                        contentDescription = "Profile picture",
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .graphicsLayer(
                                    scaleX = profilePhotoZoom,
                                    scaleY = profilePhotoZoom,
                                    translationX = profilePhotoOffsetFraction.x * avatarWidth,
                                    translationY = profilePhotoOffsetFraction.y * avatarHeight,
                                ),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile picture",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                }

                if (isEditing) {
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(
                                    Color.Black.copy(alpha = 0.35f),
                                )
                                .clickable {
                                    photoPickerLauncher.launch("image/*")
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit profile picture",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                if (isEditing) {
                    BasicTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        textStyle =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                color = nameColor,
                            ),
                        cursorBrush = SolidColor(nameColor),
                        decorationBox = { innerTextField ->
                            Column {
                                if (nameInput.isEmpty()) {
                                    Text(
                                        text = "Name",
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                        color = fadedColor,
                                    )
                                } else {
                                    innerTextField()
                                }

                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(0.55f)
                                            .height(1.dp)
                                            .background(nameColor),
                                )
                            }
                        },
                        modifier = Modifier.widthIn(min = 80.dp, max = 220.dp),
                    )
                } else {
                    Text(
                        text = nameToShow,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = nameColor,
                    )
                }

                Text(
                    text = if (isLoggedIn) "Logged in" else "Not logged in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = fadedColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLoggedIn) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { showLoginModal = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Register")
                }
            }
        }
    }

    LoginModal(
        visible = showLoginModal,
        onDismiss = { showLoginModal = false },
        authViewModel = authViewModel,
    )

    ProfilePictureEditModal(
        visible = showPfpModal,
        imageUri = tempPhotoUri,
        initialZoom = profilePhotoZoom,
        initialOffsetFraction = profilePhotoOffsetFraction,
        onDismiss = {
            showPfpModal = false
        },
        onSave = { uri, zoom, offsetFraction, containerSize, offsetPx ->
            if (uri == null) {
                showPfpModal = false
                return@ProfilePictureEditModal
            }

            coroutineScope.launch {
                authViewModel.updateProfilePhoto(
                    context = context,
                    imageUri = uri,
                    zoom = zoom,
                    containerSize = containerSize,
                    offsetPx = offsetPx,
                    onSuccess = { displayUriString ->
                        profilePhotoUri = displayUriString.toUri()
                        profilePhotoVersion++
                        profilePhotoZoom = 1f
                        profilePhotoOffsetFraction = Offset.Zero
                        showPfpModal = false
                        tempPhotoUri = null
                    },
                    onError = {
                        showPfpModal = false
                    },
                )
            }
        },
    )
}

private fun profilePhotoFile(context: android.content.Context, uid: String): File =
    File(context.filesDir, "profile_photo_${uid}.jpg")

private suspend fun fetchAndCacheProfilePhotoFromFirestore(
    context: android.content.Context,
    uid: String,
): Uri? {
    return try {
        val doc = Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("settings")
            .document("app")
            .get()
            .await()

        val url = doc.getString("profile_photo_url").orEmpty().trim()
        if (url.isBlank()) return null

        val bytes = withContext(Dispatchers.IO) {
            URL(url).openStream().use { it.readBytes() }
        }

        withContext(Dispatchers.IO) {
            val file = profilePhotoFile(context, uid)
            file.outputStream().use { it.write(bytes) }
            Uri.fromFile(file)
        }
    } catch (_: Exception) {
        null
    }
}
