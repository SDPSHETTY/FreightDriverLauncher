package com.freight.dispatch

import androidx.compose.animation.core.*
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
 * Dispatch Notification Screen - Shows load and message alerts
 * Updates automatically with new notifications
 */
@Composable
fun DispatchNotificationScreen() {
    var notification by remember { mutableStateOf("On Schedule") }
    var hasAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(10000) // Wait 10 seconds
        notification = "New Message from Dispatch"
        hasAlert = true

        delay(8000)
        notification = "Pickup Update: Delay +30 min"
        hasAlert = true

        delay(8000)
        notification = "On Schedule"
        hasAlert = false
    }

    // Pulse animation for alert
    val infiniteTransition = rememberInfiniteTransition(label = "alert")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val bgColor = if (hasAlert) Color(0xFFFF9800) else Color(0xFFF44336)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor.copy(alpha = if (hasAlert) alpha else 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Icon
            Text(
                text = if (hasAlert) "📬" else "📦",
                fontSize = 36.sp
            )

            // Notification
            Text(
                text = notification,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            // Load info
            Text(
                text = "Load: LD-2845",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Denver, CO",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            if (hasAlert) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚡ NEW",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
