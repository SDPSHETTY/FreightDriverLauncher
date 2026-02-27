package com.freight.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Navigation Notification Screen - Shows next turn information
 * Updates automatically without opening Google Maps
 */
@Composable
fun NavigationNotificationScreen() {
    // Simulate navigation updates
    var distance by remember { mutableStateOf(2.3f) }
    var instruction by remember { mutableStateOf("Continue on I-80 West") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Update every 2 seconds
            distance = (distance - 0.1f).coerceAtLeast(0f)

            if (distance < 0.5f) {
                instruction = "Turn right onto Exit 215"
                distance = 5.8f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Direction arrow
            Text(
                text = "➡️",
                fontSize = 42.sp
            )

            // Distance
            Text(
                text = "%.1f mi".format(distance),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Instruction
            Text(
                text = instruction,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            // ETA
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "ETA: 2:45 PM",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "• 187 mi",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
