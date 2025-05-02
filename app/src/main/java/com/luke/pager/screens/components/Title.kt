package com.luke.pager.screens.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Title(title: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(text = title, fontSize = 24.sp)
    }
}