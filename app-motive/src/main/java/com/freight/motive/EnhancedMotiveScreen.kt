package com.freight.motive

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
 * Enhanced Motive Screen with Integrated ELD Compliance
 * Combines full Motive Driver interface with real-time ELD monitoring
 * Includes compliance warnings and hours of service tracking
 */
@Composable
fun EnhancedMotiveScreen() {
    // ELD compliance state management
    var hoursRemaining by remember { mutableStateOf(4.6f) }
    var showComplianceWarning by remember { mutableStateOf(false) }

    // Sync with hours calculation
    val driveTimeHours = 6.4f
    val driveTimeMinutes = ((driveTimeHours - driveTimeHours.toInt()) * 60).toInt()

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Update every 10 seconds
            hoursRemaining = (hoursRemaining - 0.05f).coerceAtLeast(0f)
            showComplianceWarning = hoursRemaining < 2.0f
        }
    }

    // Warning animation
    val infiniteTransition = rememberInfiniteTransition(label = "compliance")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = if (showComplianceWarning) 0.3f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Motive Interface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E)), // Dark background like Motive app
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header with ELD status integration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Motive",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ELD Compliant",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Dynamic status indicator with ELD compliance
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (showComplianceWarning) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (showComplianceWarning) "⚠️ LOW HOURS" else "✓ ON DUTY",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main content card with integrated ELD data
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D2D2D)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Drive time with ELD compliance
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Drive Time (ELD)",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${driveTimeHours.toInt()}:${String.format("%02d", driveTimeMinutes)}:45",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Hours remaining with compliance warning
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Drive Remaining",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "%.1fh".format(hoursRemaining),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showComplianceWarning) Color(0xFFFF9800) else Color(0xFF4CAF50)
                                )
                                if (showComplianceWarning) {
                                    Text(
                                        text = "⚠️ REST SOON",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFF9800),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Shift Remaining",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "8h 15m",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Current location/route
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Current Location",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "I-80 West, Mile 245",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "En route to Denver, CO",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Enhanced demo notice with ELD compliance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1976D2).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🚛",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Enhanced Mode: Motive Driver with integrated ELD compliance monitoring",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Compliance warning overlay
        if (showComplianceWarning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFF9800).copy(alpha = warningAlpha)),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 20.sp
                        )
                        Text(
                            text = "ELD COMPLIANCE: Plan rest break within %.1f hours".format(hoursRemaining),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}