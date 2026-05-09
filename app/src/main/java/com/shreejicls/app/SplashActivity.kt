package com.shreejicls.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Saffron orange from the app's color scheme
    val saffronOrange = Color(0xFFE65100)
    val goldAccent = Color(0xFFF9A825)
    val warmWhite = Color(0xFFFFFBF5)

    // Animation states
    var startAnimation by remember { mutableStateOf(false) }

    // Logo scale animation: 0.3 -> 1.0 with overshoot
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // Logo alpha: 0 -> 1
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "logoAlpha"
    )

    // Tagline alpha: delayed fade in
    var showTagline by remember { mutableStateOf(false) }
    val taglineAlpha by animateFloatAsState(
        targetValue = if (showTagline) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = EaseInOut),
        label = "taglineAlpha"
    )

    // Subtitle alpha: even more delayed
    var showSubtitle by remember { mutableStateOf(false) }
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (showSubtitle) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseInOut),
        label = "subtitleAlpha"
    )

    // Pulsing glow ring around the logo
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Tagline slide-up offset
    val taglineOffset by animateFloatAsState(
        targetValue = if (showTagline) 0f else 30f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutCubic),
        label = "taglineOffset"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(600)
        showTagline = true
        delay(400)
        showSubtitle = true
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF1A0A00),
                        Color(0xFF0D0D0D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Glow ring behind logo
            Box(contentAlignment = Alignment.Center) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(glowScale)
                        .alpha(if (startAnimation) glowAlpha else 0f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    saffronOrange.copy(alpha = 0.5f),
                                    goldAccent.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Logo image
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "ShreeJi Classes Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main tagline
            Text(
                text = "ShreeJi Classes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = warmWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(taglineAlpha)
                    .offset(y = taglineOffset.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle tagline
            Text(
                text = "संस्कार से शिक्षा तक",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = goldAccent.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .offset(y = taglineOffset.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // English subtitle
            Text(
                text = "From Culture to Education",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = warmWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(subtitleAlpha)
                    .offset(y = taglineOffset.dp)
            )
        }
    }
}
