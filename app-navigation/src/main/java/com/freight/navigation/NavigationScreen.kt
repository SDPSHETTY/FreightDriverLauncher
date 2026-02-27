package com.freight.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight.common.AppConfig

/**
 * Navigation Screen - Route Preview & Google Maps Launcher
 * Shows route information and launches installed Google Maps app for navigation
 * No API key required!
 */
@Composable
fun NavigationScreen() {
    val context = LocalContext.current

    // Create Google Maps navigation intent
    val launchGoogleMaps = {
        try {
            // Create navigation URI for Google Maps
            val origin = "${AppConfig.Navigation.DEFAULT_ORIGIN_LAT},${AppConfig.Navigation.DEFAULT_ORIGIN_LNG}"
            val destination = "${AppConfig.Navigation.DEFAULT_DEST_LAT},${AppConfig.Navigation.DEFAULT_DEST_LNG}"

            // This URI format launches Google Maps with navigation
            val uri = Uri.parse("google.navigation:q=$destination&mode=d")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            // If Google Maps not installed, open in browser
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${AppConfig.Navigation.DEFAULT_DEST_LAT},${AppConfig.Navigation.DEFAULT_DEST_LNG}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3)), // Blue background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Title
            Text(
                text = "🧭",
                fontSize = 72.sp
            )

            Text(
                text = "Navigation",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Route card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Current Route",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "From",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = AppConfig.Navigation.ORIGIN_NAME,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = "→",
                            fontSize = 28.sp,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "To",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = AppConfig.Navigation.DEST_NAME,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Distance",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "~1,000 mi",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Est. Time",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "14h 30min",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Launch Google Maps button
            Button(
                onClick = { launchGoogleMaps() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "🗺️ Open in Google Maps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }

            // Info text
            Text(
                text = "Tap to launch full navigation in Google Maps app",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
