package com.luke.pager.screens.quotescreen.selection

import androidx.compose.ui.geometry.Offset

data class MagnifierState(
    val anchor: Offset? = null,
    val caretIndex: Int? = null,
    val isLeadingHandle: Boolean = true,
    val isActive: Boolean = false
)