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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dispatch Notification Screen - Shows load and message alerts
 * Updates automatically with new notifications
 */
@Composable
fun DispatchNotificationScreen() {
    val events = remember {
        listOf(
            "On Schedule",
            "New Message from Dispatch",
            "Pickup Update: Delay +30 min",
            "Route Confirmation Required"
        )
    }
    var eventIndex by remember { mutableStateOf(0) }
    var queueCount by remember { mutableStateOf(0) }
    var lastSync by remember { mutableStateOf(formatTimeNow()) }
    val notification = events[eventIndex]
    val hasAlert = eventIndex != 0

    LaunchedEffect(Unit) {
        while (true) {
            delay(9000)
            eventIndex = (eventIndex + 1) % events.size
            queueCount = if (eventIndex == 0) 0 else (queueCount + 1).coerceAtMost(9)
            lastSync = formatTimeNow()
        }
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

            Text(
                text = "Sync $lastSync",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            if (hasAlert) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚡ QUEUE $queueCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun formatTimeNow(): String {
    return SimpleDateFormat("HH:mm", Locale.US).format(Date())
}
