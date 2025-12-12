package com.luke.pager.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luke.pager.data.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val authError by authViewModel.authError.collectAsState()

    BackHandler { navController.popBackStack() }

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val errorToShow = localError ?: authError

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 48.dp),
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
                    .fillMaxWidth(0.9f)
                    .fillMaxSize()
                    .wrapContentHeight(Alignment.CenterVertically)
                    .offset { IntOffset(0, (-24).dp.roundToPx()) },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TransparentField(
                value = displayName,
                onValueChange = {
                    displayName = it
                    localError = null
                },
                placeholder = "Display name",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            TransparentField(
                value = email,
                onValueChange = {
                    email = it
                    localError = null
                },
                placeholder = "Email",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            TransparentField(
                value = password,
                onValueChange = {
                    password = it
                    localError = null
                },
                placeholder = "Password",
                visualTransformation = PasswordVisualTransformation(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            TransparentField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    localError = null
                },
                placeholder = "Confirm password",
                visualTransformation = PasswordVisualTransformation(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
            )

            if (!errorToShow.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorToShow,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        localError = "Passwords do not match"
                        return@Button
                    }

                    authViewModel.register(
                        email = email,
                        password = password,
                        onSuccess = {
                            authViewModel.clearError()

                            val name = displayName.trim()
                            if (name.isNotBlank()) {
                                authViewModel.updateDisplayName(
                                    context = context,
                                    newName = name,
                                    onSuccess = { navController.popBackStack() },
                                    onError = { navController.popBackStack() },
                                )
                            } else {
                                navController.popBackStack()
                            }
                        },
                    )
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
            ) {
                Text("Create account")
            }
        }
    }
}

