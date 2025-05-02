package com.luke.pager.screens.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CenteredModalScaffold(
    onDismiss: () -> Unit,
    overlayAlpha: Float,
    modifier: Modifier = Modifier,
    maxWidth: Float = 0.9f,
    topPadding: Dp = 36.dp,
    height: Dp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp() / 1.5f
    },
    background: Pair<Color, Shape> = Pair(
        MaterialTheme.colorScheme.surface,
        RoundedCornerShape(24.dp)
    ),
    sidesPadding: Dp = 24.dp,
    verticalPadding: Dp = 16.dp,
    enterFade: Int = 300,
    exitFade: Int = 200,
    visible: Boolean,
    zIndex: Float = 2f,
    content: @Composable (ScrollState) -> Unit,
) {
    val scrollState = rememberScrollState()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(enterFade)),
        exit = fadeOut(animationSpec = tween(exitFade))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
                    .clickable(
                        onClick = onDismiss,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(modifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(maxWidth)
                        .padding(top = topPadding)
                        .height(height)
                        .background(
                            color = background.first,
                            shape = background.second
                        )
                        .padding(horizontal = sidesPadding, vertical = verticalPadding)
                        .clickable(enabled = false) {}
                        .animateContentSize()
                ) {
                    content(scrollState)
                }
            }
        }
    }
}
