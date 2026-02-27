package com.freight.prepass

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Expanded PrePass Screen - Detailed weight station bypass view
 * Shows when user taps PrePass tile
 * Displays bypass authorization with station details
 */
@Composable
fun PrePassExpandedScreen() {
    // Simulate PrePass updates
    var distance by remember { mutableStateOf(47) }
    var status by remember { mutableStateOf("BYPASS") }
    var stationName by remember { mutableStateOf("CO Weigh Station - Mile Marker 215") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            distance = (distance - 1).coerceAtLeast(0)

            // Randomize bypass decision at 20 miles
            if (distance == 20) {
                status = if (Math.random() > 0.5) "BYPASS" else "MUST WEIGH IN"
            }

            // Reset for demo
            if (distance == 0) {
                distance = 47
                status = "BYPASS"
            }
        }
    }

    val isClose = distance < 10
    val bgColor = if (status == "BYPASS") Color(0xFF4CAF50) else Color(0xFFFF5722)

    // Pulsing animation when close
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Collapse hint
            Text(
                text = "⬇️ TAP TO COLLAPSE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Station name
            Text(
                text = stationName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Status indicator
            Box(
                modifier = Modifier
                    .size(if (isClose) 180.dp else 160.dp)
                    .scale(if (isClose) scale else 1f)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (status == "BYPASS") "✓" else "⚠️",
                        fontSize = 64.sp
                    )
                    Text(
                        text = status,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }
            }

            // Distance card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "$distance mi",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                    Text(
                        text = if (isClose) "APPROACHING" else "to station",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = if (isClose) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = if (status == "BYPASS") "Instructions:" else "Action Required:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (status == "BYPASS")
                            "Continue at posted speed. You have been authorized to bypass this weigh station."
                        else
                            "You must pull into the weigh station. Follow signage and pull onto scale when directed.",
                        fontSize = 13.sp,
                        color = Color.Black,
                        lineHeight = 18.sp
                    )
                }
            }

            // Vehicle info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Vehicle",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Truck #2847",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Weight",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "68,500 lbs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Active",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
