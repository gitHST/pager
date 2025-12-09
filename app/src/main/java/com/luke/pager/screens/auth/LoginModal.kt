package com.luke.pager.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.screens.components.CenteredModalScaffold

@Composable
fun LoginModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel,
) {
    val authError by authViewModel.authError.collectAsState()

    CenteredModalScaffold(
        onDismiss = {
            authViewModel.clearError()
            onDismiss()
        },
        overlayAlpha = 0.5f,
        visible = visible,
    ) { scrollState ->
        var email by remember(visible) { mutableStateOf("") }
        var password by remember(visible) { mutableStateOf("") }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (authError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.login(
                        email = email,
                        password = password,
                        onSuccess = {
                            onDismiss()
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log in")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    authViewModel.clearError()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel")
            }
        }
    }
}
