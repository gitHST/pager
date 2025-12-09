package com.luke.pager.screens.profile

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.auth.LoginModal
import com.luke.pager.screens.components.CenteredModalScaffold
import com.luke.pager.screens.components.Title

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

    val fadedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    val nameToShow = nameInput.ifBlank { "Name" }
    val nameColor =
        if (nameInput.isBlank()) fadedColor
        else MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // LEFT: Edit or Back (in edit mode)
            if (isEditing) {
                IconButton(
                    onClick = {
                        // Cancel edits: revert to current Firebase name
                        nameInput = firebaseUser?.displayName.orEmpty()
                        isEditing = false
                    },
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp, top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

            // RIGHT: Settings or Save (in edit mode)
            if (isEditing) {
                IconButton(
                    onClick = {
                        authViewModel.updateDisplayName(
                            newName = nameInput,
                            onSuccess = {
                                isEditing = false
                            },
                        )
                    },
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp, top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            } else {
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
            }

            // Centered title
            Title("Profile")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PROFILE HEADER (Avatar + Name)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile picture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )

                // Grey overlay + edit icon in edit mode
                if (isEditing) {
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                )
                                .clickable { showPfpModal = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit profile picture",
                            tint = MaterialTheme.colorScheme.onSurface,
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

    // Centered modal for profile picture editing (placeholder for now)
    CenteredModalScaffold(
        onDismiss = { showPfpModal = false },
        overlayAlpha = 0.5f,
        visible = showPfpModal,
    ) { _ ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
        ) {
            Text(
                text = "Change profile picture",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Profile picture editing coming soon.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
