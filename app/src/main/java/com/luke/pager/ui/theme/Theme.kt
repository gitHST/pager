package com.luke.pager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme =
    lightColorScheme(
        primary = PrimaryDark,
        onPrimary = Color.White,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = SecondaryGrey,
        onSecondary = Color.White,
        secondaryContainer = PrimaryContainerLight,
        onSecondaryContainer = SecondaryGrey,
        tertiary = TertiaryGold,
        onTertiary = Color.Black,
        tertiaryContainer = PrimaryContainerLight,
        onTertiaryContainer = TertiaryGold,
        background = PrimaryContainerLight,
        onBackground = PrimaryDark,
        surface = PrimaryContainerLight,
        onSurface = PrimaryDark,
        surfaceTint = Color.White,
        error = Color(0xFFB00020),
        onError = Color.White
    )

@Composable
fun PagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
