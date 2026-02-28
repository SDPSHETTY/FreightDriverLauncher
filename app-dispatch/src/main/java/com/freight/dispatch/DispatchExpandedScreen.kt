package com.freight.dispatch

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Expanded Dispatch Screen - Full WebView for dispatch system
 * Shows when user taps Dispatch tile
 * Loads configurable dispatch URL (e.g., FedEx GRDLHL Dispatch)
 */
@Composable
fun DispatchExpandedScreen(
    dispatchUrl: String = "https://fdxtools.fedex.com/grdlhldispatch",
    dispatchLoginUrl: String? = null,
    autoLoginRedirect: Boolean = false
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasRedirectedToLogin by remember { mutableStateOf(false) }

    val normalizedDispatchUrl = remember(dispatchUrl) { dispatchUrl.trim() }
    val normalizedLoginUrl = remember(dispatchLoginUrl) {
        dispatchLoginUrl?.trim()?.takeIf { it.isNotBlank() }
    }
    val initialUrl = remember(normalizedDispatchUrl, normalizedLoginUrl, autoLoginRedirect) {
        if (autoLoginRedirect && normalizedLoginUrl != null) normalizedLoginUrl else normalizedDispatchUrl
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF44336))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with collapse hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF424242))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⬇️ TAP TO COLLAPSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📦 Dispatch System",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // WebView container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)

                                    if (
                                        autoLoginRedirect &&
                                        !hasRedirectedToLogin &&
                                        normalizedLoginUrl != null &&
                                        shouldRedirectToLogin(
                                            currentUrl = url,
                                            dispatchUrl = normalizedDispatchUrl,
                                            loginUrl = normalizedLoginUrl
                                        )
                                    ) {
                                        hasRedirectedToLogin = true
                                        view?.loadUrl(normalizedLoginUrl)
                                        return
                                    }

                                    isLoading = false
                                }
                            }

                            loadUrl(initialUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Loading indicator
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFF44336),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Loading Dispatch System...",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shouldRedirectToLogin(currentUrl: String?, dispatchUrl: String, loginUrl: String): Boolean {
    if (currentUrl.isNullOrBlank()) {
        return false
    }

    val currentUri = runCatching { Uri.parse(currentUrl) }.getOrNull() ?: return false
    val dispatchUri = runCatching { Uri.parse(dispatchUrl) }.getOrNull()
    val loginUri = runCatching { Uri.parse(loginUrl) }.getOrNull()

    if (loginUri != null && currentUri.toString().startsWith(loginUri.toString(), ignoreCase = true)) {
        return false
    }

    if (dispatchUri == null) {
        return false
    }

    val sameHost = currentUri.host.equals(dispatchUri.host, ignoreCase = true)
    if (!sameHost) {
        return false
    }

    val currentPath = currentUri.path.orEmpty().trim()
    val dispatchPath = dispatchUri.path.orEmpty().trim()

    return currentPath.isBlank() || currentPath == "/" || currentPath == dispatchPath
}
