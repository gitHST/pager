package com.luke.pager.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

val LocalUseDarkTheme = staticCompositionLocalOf { false }
