package com.luke.pager.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.luke.pager.auth.mapAuthErrorToUserMessage
import com.luke.pager.data.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onShowSnackbar: (String) -> Unit,
) {
    val authError by authViewModel.authError.collectAsState()

    BackHandler { navController.popBackStack() }

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // ONLY for password/name validation
    var inlineError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authError) {
        val msg = authError
        if (!msg.isNullOrBlank()) {
            onShowSnackbar(mapAuthErrorToUserMessage(msg))
            authViewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Back arrow (top-left)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Push title near top
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Create account",
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.widthIn(max = 520.dp),
            )

            Spacer(modifier = Modifier.height(96.dp))

            // Fields block (centered horizontally)
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TransparentField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        inlineError = null
                    },
                    placeholder = "Display name",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                TransparentField(
                    value = email,
                    onValueChange = {
                        email = it
                        inlineError = null
                    },
                    placeholder = "Email",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                TransparentField(
                    value = password,
                    onValueChange = {
                        password = it
                        inlineError = null
                    },
                    placeholder = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(14.dp))

                TransparentField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        inlineError = null
                    },
                    placeholder = "Confirm password",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!inlineError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inlineError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        val trimmedName = displayName.trim()
                        val trimmedEmail = email.trim()

                        inlineError =
                            when {
                                trimmedName.isBlank() -> "Display name can't be empty"
                                password.length < 6 -> "Password must be at least 6 characters"
                                password != confirmPassword -> "Passwords do not match"
                                else -> null
                            }

                        if (inlineError != null) return@Button

                        authViewModel.register(
                            email = trimmedEmail,
                            password = password,
                            onSuccess = {
                                authViewModel.clearError()
                                navController.popBackStack()
                            },
                            onError = { msg ->
                                onShowSnackbar(mapAuthErrorToUserMessage(msg))
                                authViewModel.clearError()
                            },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Create account")
                }
            }
        }
    }
}
