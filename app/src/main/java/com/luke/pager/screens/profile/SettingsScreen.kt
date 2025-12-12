package com.luke.pager.screens.profile

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.data.viewmodel.BookViewModel
import com.luke.pager.data.viewmodel.QuoteViewModel
import com.luke.pager.data.viewmodel.ReviewViewModel
import com.luke.pager.screens.components.Title
import com.luke.pager.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel,
    authViewModel: AuthViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    syncOverCellular: Boolean,
    onSyncOverCellularChange: (Boolean) -> Unit,
) {
    val themeOptions =
        listOf(
            ThemeMode.LIGHT to "Light",
            ThemeMode.DARK to "Dark",
            ThemeMode.SYSTEM to "System default",
        )

    var expanded by remember { mutableStateOf(false) }

    val selectedLabel =
        themeOptions.firstOrNull { it.first == themeMode }?.second ?: "System default"

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firebaseUser = Firebase.auth.currentUser

    var showExportDialog by remember { mutableStateOf(false) }
    var exportEmail by remember { mutableStateOf("") }
    var exportError by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var reauthError by remember { mutableStateOf<String?>(null) }

    fun startExport(toEmail: String) {
        coroutineScope.launch {
            isExporting = true
            exportError = null
            try {
                val json = buildExportJson(bookViewModel, reviewViewModel, quoteViewModel)
                sendExportEmail(context, toEmail.trim(), json)
                showExportDialog = false
            } catch (e: Exception) {
                exportError = e.message ?: "Failed to export data"
            } finally {
                isExporting = false
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
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

            Title("Settings")
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Appearance",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    Box(
                        modifier =
                            Modifier
                                .menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                    enabled = true,
                                ).fillMaxWidth(0.55f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { expanded = true }
                                .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = selectedLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        themeOptions.forEach { (mode, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onThemeModeChange(mode)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Data",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            onSyncOverCellularChange(!syncOverCellular)
                        },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Sync data over cellular",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Switch(
                    checked = syncOverCellular,
                    onCheckedChange = { checked ->
                        onSyncOverCellularChange(checked)
                    },
                    modifier = Modifier.scale(0.8f),
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val defaultEmail = firebaseUser?.email
                            if (defaultEmail.isNullOrBlank()) {
                                exportEmail = ""
                                showExportDialog = true
                            } else {
                                exportEmail = defaultEmail
                                showExportDialog = true
                            }
                        },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Export data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Account",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (firebaseUser != null && !firebaseUser.isAnonymous) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                showLogoutDialog = true
                            },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                showDeleteDialog = true
                            },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Delete account and data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isExporting) {
                    showExportDialog = false
                }
            },
            title = { Text("Export data") },
            text = {
                Column {
                    Text(
                        text = "Enter an email address to send your data export as JSON.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportEmail,
                        onValueChange = { exportEmail = it },
                        singleLine = true,
                        label = { Text("Email") },
                    )
                    if (exportError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exportError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isExporting && exportEmail.isNotBlank()) {
                            startExport(exportEmail)
                        }
                    },
                    enabled = !isExporting && exportEmail.isNotBlank(),
                ) {
                    Text(if (isExporting) "Exporting..." else "Export")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isExporting) showExportDialog = false },
                    enabled = !isExporting,
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logout(context)
                        showLogoutDialog = false
                        navController.popBackStack()
                    },
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showReauthDialog) {
        val user = firebaseUser
        val isPasswordUser = user?.providerData?.any { it.providerId == "password" } == true
        val activity = (LocalContext.current as? Activity)

        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showReauthDialog = false
                    reauthPassword = ""
                    reauthError = null
                }
            },
            title = { Text("Confirm identity") },
            text = {
                Column {
                    Text(
                        text = "Please enter your password",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isPasswordUser) {
                        OutlinedTextField(
                            value = reauthPassword,
                            onValueChange = {
                                reauthPassword = it
                                reauthError = null
                            },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                    } else {
                        Text(
                            text = "Continue with Google to confirm.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    if (reauthError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reauthError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val current =
                            user ?: run {
                                reauthError = "Not logged in"
                                return@TextButton
                            }

                        reauthError = null
                        isDeleting = true
                        deleteError = null

                        val runDelete = {
                            authViewModel.deleteAccountAndData(
                                context = context,
                                onSuccess = {
                                    isDeleting = false
                                    showReauthDialog = false
                                    navController.popBackStack()
                                },
                                onError = { msg ->
                                    isDeleting = false
                                    reauthError = msg
                                },
                            )
                        }

                        if (isPasswordUser) {
                            val email = current.email.orEmpty()
                            if (email.isBlank() || reauthPassword.isBlank()) {
                                isDeleting = false
                                reauthError = "Enter your password"
                                return@TextButton
                            }

                            authViewModel.reauthenticateWithPassword(
                                email = email,
                                password = reauthPassword,
                                onSuccess = { runDelete() },
                                onError = { msg ->
                                    isDeleting = false
                                    reauthError = msg
                                },
                            )
                        } else {
                            if (activity == null) {
                                isDeleting = false
                                reauthError = "No Activity available for Google re-auth"
                                return@TextButton
                            }

                            authViewModel.reauthenticateWithGoogle(
                                activity = activity,
                                onSuccess = { runDelete() },
                                onError = { msg ->
                                    isDeleting = false
                                    reauthError = msg
                                },
                            )
                        }
                    },
                    enabled = !isDeleting,
                ) {
                    Text(
                        if (isDeleting) {
                            "Deleting..."
                        } else if (isPasswordUser) {
                            "Confirm"
                        } else {
                            "Continue"
                        },
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isDeleting) {
                            showReauthDialog = false
                            reauthPassword = ""
                            reauthError = null
                        }
                    },
                    enabled = !isDeleting,
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) showDeleteDialog = false
            },
            title = { Text("Delete account and data") },
            text = {
                Column {
                    Text(
                        text =
                            "This will permanently delete your account, all your books, " +
                                "quotes, reviews, and profile data. This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (deleteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = deleteError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        reauthPassword = ""
                        reauthError = null
                        showReauthDialog = true
                    },
                    enabled = !isDeleting,
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isDeleting) showDeleteDialog = false },
                    enabled = !isDeleting,
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

private suspend fun buildExportJson(
    bookViewModel: BookViewModel,
    reviewViewModel: ReviewViewModel,
    quoteViewModel: QuoteViewModel,
): String =
    withContext(Dispatchers.Default) {
        val books = bookViewModel.books.value
        val quotes = quoteViewModel.quotes.value
        val reviews = reviewViewModel.reviews.value

        val payload =
            mapOf(
                "books" to books,
                "quotes" to quotes,
                "reviews" to reviews,
            )

        Gson().toJson(payload)
    }

private fun sendExportEmail(
    context: android.content.Context,
    toEmail: String,
    json: String,
) {
    val sendIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            putExtra(Intent.EXTRA_SUBJECT, "Your Pager data export")
            putExtra(Intent.EXTRA_TEXT, json)
        }

    val chooser = Intent.createChooser(sendIntent, "Send data export")

    if (sendIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(chooser)
    } else {
        throw IllegalStateException(
            "No app found that can send email. Install an email app and try again.",
        )
    }
}
