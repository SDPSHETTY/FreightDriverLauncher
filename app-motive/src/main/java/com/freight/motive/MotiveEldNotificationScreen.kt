package com.freight.motive

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
 * Motive ELD Notification Screen - Combined ELD compliance monitoring
 * Integrated into Motive Driver for unified experience
 * Shows hours of service alerts with Motive branding
 */
@Composable
fun MotiveEldNotificationScreen() {
    var hoursRemaining by remember { mutableStateOf(4.6f) }
    var showWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Update every 5 seconds
            hoursRemaining = (hoursRemaining - 0.1f).coerceAtLeast(0f)
            showWarning = hoursRemaining < 2.0f
        }
    }

    // Pulse animation for warning
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val bgColor = if (showWarning) Color(0xFFFF9800) else Color(0xFF4CAF50)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor.copy(alpha = if (showWarning) alpha else 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Motive branding for ELD
            Text(
                text = "🚛",
                fontSize = 32.sp
            )

            Text(
                text = "MOTIVE ELD",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status icon
            Text(
                text = if (showWarning) "⚠️" else "✓",
                fontSize = 36.sp
            )

            // Status
            Text(
                text = if (showWarning) "LOW HOURS" else "ON DUTY - DRIVING",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Hours remaining
            Text(
                text = "%.1f hrs".format(hoursRemaining),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Drive Time Left",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            if (showWarning) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚡ Plan rest break",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}