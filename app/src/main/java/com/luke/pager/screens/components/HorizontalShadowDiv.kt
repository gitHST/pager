package com.luke.pager.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.luke.pager.ui.theme.LocalUseDarkTheme

@Composable
fun HorizontalShadowDiv(
    modifier: Modifier = Modifier,
    shadowFacingUp: Boolean = false,
    hozPadding: Float = 0f,
    visible: Boolean = true,
    hideLine: Boolean = false
) {
    val isDarkTheme = LocalUseDarkTheme.current

    val outlineShadow = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val shadowColor = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.65f)
    } else {
        outlineShadow
    }

    val lineColor = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.8f)
    } else {
        outlineShadow
    }

    Column(modifier = modifier.padding(horizontal = hozPadding.dp)) {

        if (shadowFacingUp) {

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    shadowColor
                                )
                            )
                        )
                )
            }

            if (!hideLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(lineColor)
                )
            }

        } else {

            if (!hideLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(lineColor)
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    shadowColor,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}
