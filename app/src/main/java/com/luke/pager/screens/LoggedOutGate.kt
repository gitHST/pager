package com.luke.pager.screens.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luke.pager.R
import com.luke.pager.auth.mapAuthErrorToUserMessage
import com.luke.pager.data.viewmodel.AuthViewModel
import com.luke.pager.network.onlineStatusFlow
import com.luke.pager.ui.theme.BackgroundDark
import com.luke.pager.ui.theme.BackgroundLight
import com.luke.pager.ui.theme.LocalUseDarkTheme
import com.luke.pager.ui.theme.PagerTheme
import kotlinx.coroutines.launch

private val CaslonPro = FontFamily(Font(R.font.caslonpro, FontWeight.Normal))
private val PagerTitleColorLight = Color(0xFF63503A)
private val PagerTitleColorDark = Color(0xFFF6EDDB)

@Composable
fun LoggedOutGate(authViewModel: AuthViewModel) {
    val systemIsDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Re-usable online state (updates every 10s)
    val isOnline by onlineStatusFlow(context, intervalMs = 10_000L)
        .collectAsState(initial = true)

    fun showSnackbar(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    CompositionLocalProvider(LocalUseDarkTheme provides systemIsDark) {
        PagerTheme(useDarkTheme = systemIsDark) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { paddingValues ->
                // ✅ Background draws edge-to-edge (no scaffold padding applied here)
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(if (systemIsDark) BackgroundDark else BackgroundLight),
                    )

                    Image(
                        painter = painterResource(id = R.drawable.clean_gray_paper),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .alpha(if (systemIsDark) 0.1f else 0.9f),
                    )

                    // ✅ Only the actual content respects scaffold insets/snackbar/etc
                    val navController = rememberNavController()

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "login",
                        ) {
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    isOnline = isOnline,
                                    onShowSnackbar = ::showSnackbar,
                                    onSignUpClick = { navController.navigate("register") },
                                )
                            }

                            composable("register") {
                                RegisterScreen(
                                    navController = navController,
                                    authViewModel = authViewModel,
                                    onShowSnackbar = ::showSnackbar,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    authViewModel: AuthViewModel,
    isOnline: Boolean,
    onShowSnackbar: (String) -> Unit,
    onSignUpClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val textColor = MaterialTheme.colorScheme.onBackground
    val systemIsDark = isSystemInDarkTheme()
    val titleColor = if (systemIsDark) PagerTitleColorDark else PagerTitleColorLight

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val titleTopSpacer = maxHeight * 0.25f

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .wrapContentSize(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(titleTopSpacer))

                Text(
                    text = "Pager",
                    fontFamily = CaslonPro,
                    fontSize = 60.sp,
                    color = titleColor,
                )

                Spacer(modifier = Modifier.height(30.dp))

                TransparentField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                TransparentField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            authViewModel.login(
                                email = email,
                                password = password,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Sign in")
                    }

                    val activity = LocalContext.current as Activity
                    IconButton(
                        onClick = {
                            if (!isOnline) {
                                onShowSnackbar("No internet connection")
                                return@IconButton
                            }

                            authViewModel.signInWithGoogle(
                                activity = activity,
                                onSuccess = {},
                                onError = { msg ->
                                    // Keep Google errors out of inline Register UI
                                    onShowSnackbar(mapAuthErrorToUserMessage(msg))
                                    authViewModel.clearError()
                                },
                            )
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    id =
                                        if (systemIsDark) {
                                            R.drawable.ic_google_dark
                                        } else {
                                            R.drawable.ic_google_light
                                        },
                                ),
                            contentDescription = "Sign in with Google",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                val signUpText =
                    buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        ) {
                            append("Sign up")
                        }
                    }

                Text(
                    text = signUpText,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { onSignUpClick() },
                )
            }
        }
    }
}

@Composable
fun TransparentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val hintColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

    Box(
        modifier = modifier.height(56.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = visualTransformation,
            textStyle =
                MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontSize = 18.sp,
                ),
            decorationBox = { inner ->
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = hintColor,
                        fontSize = 18.sp,
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
