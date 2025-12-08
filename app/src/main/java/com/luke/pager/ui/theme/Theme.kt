package com.luke.pager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
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
        background = BackgroundLight,
        onBackground = PrimaryDark,
        surface = PrimaryContainerLight,
        onSurface = PrimaryDark,
        surfaceTint = Color.White,
        error = Color(0xFFB00020),
        onError = Color.White
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = NiceBlue,
        onPrimary = Color.Black,
        primaryContainer = PrimaryDarkContainer,
        onPrimaryContainer = OnPrimaryDark,
        secondary = SecondaryGreyDark,
        onSecondary = Color.Black,
        secondaryContainer = PrimaryDarkContainer,
        onSecondaryContainer = SecondaryGreyDark,
        tertiary = TertiaryGoldDark,
        onTertiary = Color.Black,
        tertiaryContainer = PrimaryDarkContainer,
        onTertiaryContainer = TertiaryGoldDark,
        background = BackgroundDark,
        onBackground = OnPrimaryDark,
        surface = PrimaryDarkSurface,
        onSurface = OnPrimaryDark,
        surfaceTint = Color.Black,
        error = Color(0xFFCF6679),
        onError = Color.Black
    )
@Composable
fun PagerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
