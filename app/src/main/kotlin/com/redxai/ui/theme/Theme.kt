package com.redxai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RedxColorScheme = darkColorScheme(
    primary = RedxRed,
    onPrimary = RedxTextPrimary,
    primaryContainer = RedxRedDim,
    onPrimaryContainer = RedxTextPrimary,
    secondary = RedxSurfaceVariant,
    onSecondary = RedxTextPrimary,
    secondaryContainer = RedxSurfaceVariant,
    onSecondaryContainer = RedxTextSecondary,
    background = RedxBackground,
    onBackground = RedxTextPrimary,
    surface = RedxSurface,
    onSurface = RedxTextPrimary,
    surfaceVariant = RedxSurfaceVariant,
    onSurfaceVariant = RedxTextSecondary,
    outline = RedxBorder,
    error = RedxRedBright,
    onError = RedxTextPrimary,
)

@Composable
fun RedxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RedxColorScheme,
        typography = RedxTypography,
        content = content
    )
}

val RedxTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 10.sp),
)
