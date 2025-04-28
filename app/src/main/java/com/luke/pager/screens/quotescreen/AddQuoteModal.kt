package com.luke.pager.screens.quotescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.luke.pager.data.viewmodel.QuoteViewModel

@Composable
fun AddQuoteModal(onDismiss: () -> Unit, quoteViewModel: QuoteViewModel, navController: NavController) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val modalHeight = screenHeight / 1.5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss, indication = null, interactionSource = remember { MutableInteractionSource() })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .padding(top = 36.dp)
                .height(modalHeight) // <- Fixed height to take exactly 1.5f of screen
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header Placeholder
            Text("Add Quote", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            // Content Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Quote Input Area", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Save Button", color = Color.White)
            }
        }
    }
}
