package com.redxai.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redxai.ui.theme.*

@Composable
fun HomeScreen(
    onOpenChat: () -> Unit,
    onOpenBuilder: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RedxBackground)
    ) {
        // Subtle red glow at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(RedxRed.copy(alpha = 0.08f), Color.Transparent),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo / Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REDX",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = RedxRed,
                    letterSpacing = 8.sp
                )
                Text(
                    text = "AI",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = RedxTextPrimary,
                    letterSpacing = 8.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Uncensored · Unrestricted · Unlimited",
                    fontSize = 12.sp,
                    color = RedxTextMuted,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(64.dp))

            // Main action cards
            HomeCard(
                icon = Icons.Default.Chat,
                title = "AI Chat",
                subtitle = "Chat with uncensored AI. No limits, no filters.",
                accentColor = RedxRed,
                onClick = onOpenChat
            )

            Spacer(Modifier.height(16.dp))

            HomeCard(
                icon = Icons.Default.Build,
                title = "APK Builder",
                subtitle = "Describe an app → AI generates & compiles the APK via GitHub.",
                accentColor = RedxBlue,
                onClick = onOpenBuilder
            )

            Spacer(Modifier.height(16.dp))

            HomeCard(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "API keys · GitHub · Firebase · Model selection",
                accentColor = RedxTextSecondary,
                onClick = onOpenSettings
            )

            Spacer(Modifier.weight(1f))

            // Footer
            Text(
                text = "Powered by OpenRouter · dolphin-llama-3-70b & more",
                fontSize = 10.sp,
                color = RedxTextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RedxSurface)
            .border(1.dp, RedxBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = RedxTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RedxTextMuted, modifier = Modifier.size(20.dp))
        }
    }
}
