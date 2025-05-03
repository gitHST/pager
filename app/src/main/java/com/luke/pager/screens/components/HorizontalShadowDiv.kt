package com.luke.pager.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalShadowDiv(shadowFacingUp: Boolean = false, hozPadding: Float = 0f, modifier: Modifier = Modifier) {
    val gradientColors = if (shadowFacingUp) {
        listOf(Color.Transparent, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    } else {
        listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), Color.Transparent)
    }

    Column(modifier = modifier.padding(horizontal = hozPadding.dp)) {
        if (shadowFacingUp) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = gradientColors
                        )
                    )
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = gradientColors
                        )
                    )
            )
        }
    }
}
