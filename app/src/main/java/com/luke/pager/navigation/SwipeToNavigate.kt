package com.luke.pager.navigation

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController

@Composable
fun SwipeToNavigate(
    navController: NavHostController,
    currentRoute: String,
    navItems: List<NavItem>,
    content: @Composable (currentRoute: String, navItems: List<NavItem>) -> Unit
) {
    val currentIndex = navItems.indexOfFirst { it.route == currentRoute }
    val setDragAmount = 50

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { change, dragAmount ->
                if (dragAmount > setDragAmount) {
                    val previousIndex = (currentIndex - 1).coerceAtLeast(0)
                    navController.navigate(navItems[previousIndex].route)
                } else if (dragAmount < -setDragAmount) {
                    val nextIndex = (currentIndex + 1).coerceAtMost(navItems.lastIndex)
                    navController.navigate(navItems[nextIndex].route)
                }
            }
        }
    ) {
        content(currentRoute, navItems)
    }
}
