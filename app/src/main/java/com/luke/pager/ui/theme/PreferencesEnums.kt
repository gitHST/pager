package com.luke.pager.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

enum class DiaryLayout {
    COMPACT,
    EXPANDED,
}

val LocalUseDarkTheme = staticCompositionLocalOf { false }
