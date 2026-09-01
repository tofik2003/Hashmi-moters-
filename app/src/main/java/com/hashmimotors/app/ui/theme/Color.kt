package com.hashmimotors.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Brand palette — premium "midnight indigo & gold" identity
// ============================================================
val BrandIndigo = Color(0xFF6366F1)
val BrandIndigoDark = Color(0xFF312E81)
val BrandIndigoLight = Color(0xFFC7D2FE)
val BrandPurple = Color(0xFF8B5CF6)
val BrandTeal = Color(0xFF14B8A6)
val BrandAmber = Color(0xFFF59E0B)

// Accent palette (user-selectable)
val AccentIndigo = Color(0xFF6366F1)
val AccentBlue = Color(0xFF3B82F6)
val AccentGreen = Color(0xFF10B981)
val AccentOrange = Color(0xFFF97316)
val AccentRose = Color(0xFFF43F5E)
val AccentTeal = Color(0xFF14B8A6)
val AccentViolet = Color(0xFF8B5CF6)
val AccentGold = Color(0xFFF5B942)

// Premium accent highlights
val PremiumGold = Color(0xFFF5B942)
val PremiumGoldDeep = Color(0xFFE0A12E)
val PremiumCyan = Color(0xFF22D3EE)
val PremiumRose = Color(0xFFFB7185)

// ============================================================
// Background gradients
// ============================================================
// Deep premium gradient (default dark)
val GradientStart = Color(0xFF141433)
val GradientMiddle = Color(0xFF1E1B5E)
val GradientEnd = Color(0xFF3B1E7A)

// "Aurora" gradient for Light theme mode (bright, still dark enough for white text)
val AuroraStart = Color(0xFF2B2B6E)
val AuroraMiddle = Color(0xFF4630A6)
val AuroraEnd = Color(0xFF6D28D9)

fun premiumBackgroundColors(dark: Boolean): List<Color> =
    if (dark) listOf(GradientStart, GradientMiddle, GradientEnd)
    else listOf(AuroraStart, AuroraMiddle, AuroraEnd)

// Confetti colors
val ConfettiColors = listOf(
    Color(0xFFFF6B6B),
    Color(0xFF4ECDC4),
    Color(0xFFFFE66D),
    Color(0xFF95E1D3),
    Color(0xFFF38181),
    Color(0xFFAA96DA),
    Color(0xFFFFA000),
    Color(0xFF8B5CF6),
    Color(0xFFF5B942),
    Color(0xFF22D3EE)
)

// ============================================================
// Light scheme
// ============================================================
val LightPrimary = Color(0xFF4F46E5)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE0E7FF)
val LightOnPrimaryContainer = Color(0xFF1E1B4B)
val LightSecondary = Color(0xFF5B5D72)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE0E1F9)
val LightOnSecondaryContainer = Color(0xFF171A2C)
val LightTertiary = Color(0xFF6D3D8F)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFF2DAFF)
val LightOnTertiaryContainer = Color(0xFF2A0E40)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)
val LightBackground = Color(0xFFF7F8FF)
val LightOnBackground = Color(0xFF1B1B2F)
val LightSurface = Color(0xFFFCFBFF)
val LightOnSurface = Color(0xFF1B1B2F)
val LightSurfaceVariant = Color(0xFFE3E2F0)
val LightOnSurfaceVariant = Color(0xFF46464F)
val LightOutline = Color(0xFF77768E)
val LightOutlineVariant = Color(0xFFC7C6D0)

// ============================================================
// Dark scheme
// ============================================================
val DarkPrimary = Color(0xFFA5B4FC)
val DarkOnPrimary = Color(0xFF1E1B4B)
val DarkPrimaryContainer = Color(0xFF4F46E5)
val DarkOnPrimaryContainer = Color(0xFFE0E7FF)
val DarkSecondary = Color(0xFFC2C5DD)
val DarkOnSecondary = Color(0xFF2C2F42)
val DarkSecondaryContainer = Color(0xFF424659)
val DarkOnSecondaryContainer = Color(0xFFE0E1F9)
val DarkTertiary = Color(0xFFE5B5EB)
val DarkOnTertiary = Color(0xFF44244B)
val DarkTertiaryContainer = Color(0xFF5C3A63)
val DarkOnTertiaryContainer = Color(0xFFF2DAFF)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkBackground = Color(0xFF13131F)
val DarkOnBackground = Color(0xFFE4E4F0)
val DarkSurface = Color(0xFF17172A)
val DarkOnSurface = Color(0xFFE4E4F0)
val DarkSurfaceVariant = Color(0xFF46464F)
val DarkOnSurfaceVariant = Color(0xFFC7C6D0)
val DarkOutline = Color(0xFF90909A)
val DarkOutlineVariant = Color(0xFF46464F)

// Status colors
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)
val StatusInfo = Color(0xFF3B82F6)
