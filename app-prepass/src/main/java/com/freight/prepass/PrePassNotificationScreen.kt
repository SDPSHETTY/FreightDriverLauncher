package com.freight.prepass

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
 * PrePass Notification Screen - Shows weight station alerts
 * Updates automatically as driver approaches stations
 */
@Composable
fun PrePassNotificationScreen() {
    val stationDistances = remember { listOf(47, 31, 20, 8, 3) }
    val statuses = remember { listOf("BYPASS", "BYPASS", "MUST WEIGH IN", "BYPASS", "BYPASS") }
    var checkpointIndex by remember { mutableStateOf(0) }
    var distance by remember { mutableStateOf(stationDistances.first()) }
    var status by remember { mutableStateOf(statuses.first()) }

    // Countdown distance
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            checkpointIndex = (checkpointIndex + 1) % stationDistances.size
            distance = stationDistances[checkpointIndex]
            status = statuses[checkpointIndex]

            if (checkpointIndex == stationDistances.lastIndex) {
                delay(2500)
            }
        }
    }

    // Pulse animation for alert
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val bgColor = if (status == "BYPASS") Color(0xFF4CAF50) else Color(0xFFF44336)
    val isClose = distance < 10

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor.copy(alpha = if (isClose) alpha else 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Station icon
            Text(
                text = "⚠️",
                fontSize = 36.sp
            )

            // Status
            Text(
                text = status,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Distance
            Text(
                text = "$distance miles",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Station info
            Text(
                text = "Weigh Station Ahead",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            if (isClose) {
                Text(
                    text = "⚡ APPROACHING",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
