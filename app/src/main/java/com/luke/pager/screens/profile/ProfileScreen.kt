package com.luke.pager.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.auth.LoginModal
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.components.Title
import kotlin.math.min

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

    val firebaseUser = Firebase.auth.currentUser

    var nameInput by remember(firebaseUser?.uid) {
        mutableStateOf(firebaseUser?.displayName.orEmpty())
    }

    var profilePhotoUri by remember(firebaseUser?.uid) {
        mutableStateOf<Uri?>(null)
    }
    var profilePhotoZoom by remember(firebaseUser?.uid) {
        mutableFloatStateOf(1f)
    }
    var profilePhotoOffsetFraction by remember(firebaseUser?.uid) {
        mutableStateOf(Offset.Zero)
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

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

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isEditing) {
                IconButton(
                    onClick = {
                        authViewModel.updateDisplayName(
                            newName = nameInput,
                            onSuccess = { isEditing = false }
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
                if (profilePhotoUri != null) {
                    val avatarWidth = avatarSize.width.toFloat().coerceAtLeast(1f)
                    val avatarHeight = avatarSize.height.toFloat().coerceAtLeast(1f)

                    Image(
                        painter = rememberAsyncImagePainter(profilePhotoUri),
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
        onSave = { uri, zoom, offsetFraction ->
            if (uri != null) {
                profilePhotoUri = uri
                profilePhotoZoom = zoom
                profilePhotoOffsetFraction = offsetFraction
            }
            showPfpModal = false
        },
    )
}

@Composable
private fun ProfilePictureEditModal(
    visible: Boolean,
    imageUri: Uri?,
    initialZoom: Float,
    initialOffsetFraction: Offset,
    onDismiss: () -> Unit,
    onSave: (Uri?, Float, Offset) -> Unit,
) {
    var zoom by remember(visible) { mutableFloatStateOf(initialZoom.coerceIn(1f, 3f)) }
    var offsetPx by remember(visible) { mutableStateOf(Offset.Zero) }
    var offsetFraction by remember(visible) { mutableStateOf(initialOffsetFraction) }
    var containerSize by remember(visible) { mutableStateOf(IntSize.Zero) }
    var initializedFromFraction by remember(visible) { mutableStateOf(false) }

    var isInteracting by remember(visible) { mutableStateOf(false) }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cropOverlayAlpha",
    )

    fun clampOffset(raw: Offset, scale: Float): Offset {
        val width = containerSize.width.toFloat()
        val height = containerSize.height.toFloat()

        if (width == 0f || height == 0f) return raw

        if (scale <= 1f) return Offset.Zero

        val radius = min(width, height) * 0.5f

        val halfWidthScaled = width * scale / 2f
        val halfHeightScaled = height * scale / 2f

        val minX = radius - halfWidthScaled
        val maxX = halfWidthScaled - radius

        val minY = radius - halfHeightScaled
        val maxY = halfHeightScaled - radius

        return Offset(
            x = raw.x.coerceIn(minX, maxX),
            y = raw.y.coerceIn(minY, maxY),
        )
    }

    LaunchedEffect(containerSize, visible) {
        val width = containerSize.width.toFloat()
        val height = containerSize.height.toFloat()

        if (visible && !initializedFromFraction && width > 0f && height > 0f) {
            offsetPx = clampOffset(
                Offset(
                    x = offsetFraction.x * width,
                    y = offsetFraction.y * height,
                ),
                zoom,
            )
            initializedFromFraction = true
        }
    }

    LaunchedEffect(zoom) {
        offsetPx = clampOffset(offsetPx, zoom)
    }

    CenteredModalScaffold(
        onDismiss = onDismiss,
        overlayAlpha = 0.5f,
        visible = visible,
    ) { _ ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
        ) {
            if (imageUri != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(0.95f)
                                .aspectRatio(1f)
                                .onSizeChanged { containerSize = it }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            isInteracting = true
                                        },
                                        onDragEnd = {
                                            isInteracting = false
                                        },
                                        onDragCancel = {
                                            isInteracting = false
                                        },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        val newOffset = offsetPx + dragAmount
                                        offsetPx = clampOffset(newOffset, zoom)
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = CircleShape,
                                    ),
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Profile picture preview",
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .graphicsLayer {
                                            scaleX = zoom
                                            scaleY = zoom
                                            translationX = offsetPx.x
                                            translationY = offsetPx.y
                                        },
                                contentScale = ContentScale.Crop,
                            )

                            Canvas(
                                modifier = Modifier.matchParentSize(),
                            ) {
                                if (overlayAlpha > 0.01f) {
                                    val overlayColor =
                                        Color.Black.copy(alpha = 0.35f * overlayAlpha)
                                    val lineColor =
                                        Color.White.copy(alpha = 0.6f * overlayAlpha)
                                    val stroke = Stroke(width = 1.dp.toPx())

                                    drawRect(color = overlayColor)

                                    val w = size.width
                                    val h = size.height

                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = w / 3f, y = 0f),
                                        end = Offset(x = w / 3f, y = h),
                                        strokeWidth = stroke.width,
                                    )
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = 2f * w / 3f, y = 0f),
                                        end = Offset(x = 2f * w / 3f, y = h),
                                        strokeWidth = stroke.width,
                                    )

                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = 0f, y = h / 3f),
                                        end = Offset(x = w, y = h / 3f),
                                        strokeWidth = stroke.width,
                                    )
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(x = 0f, y = 2f * h / 3f),
                                        end = Offset(x = w, y = 2f * h / 3f),
                                        strokeWidth = stroke.width,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = zoom,
                    onValueChange = {
                        isInteracting = true
                        zoom = it.coerceIn(1f, 3f)
                    },
                    onValueChangeFinished = {
                        isInteracting = false
                    },
                    valueRange = 1f..3f,
                )
            } else {
                Text(
                    text = "No image selected.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val width = containerSize.width.toFloat().coerceAtLeast(1f)
                        val height = containerSize.height.toFloat().coerceAtLeast(1f)

                        val newFraction = if (width > 0f && height > 0f) {
                            Offset(
                                x = offsetPx.x / width,
                                y = offsetPx.y / height,
                            )
                        } else {
                            offsetFraction
                        }

                        offsetFraction = newFraction
                        onSave(imageUri, zoom, newFraction)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = imageUri != null,
                ) {
                    Text("Save")
                }
            }
        }
    }
}
