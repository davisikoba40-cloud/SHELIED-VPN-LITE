package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ShieldDarkColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = ShieldBlue,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = ShieldGreen,
    onTertiary = Color(0xFF022C22),
    background = ShieldNavyDark,
    onBackground = TextPrimary,
    surface = ShieldSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = ShieldSurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderHighlight,
    error = ShieldRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Use our signature Shield security cyber palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShieldDarkColorScheme,
        typography = Typography,
        content = content
    )
}
