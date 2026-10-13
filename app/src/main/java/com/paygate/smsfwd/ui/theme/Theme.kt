package com.paygate.smsfwd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Gate UI Material 3 Color Schemes derived from DESIGN.md / App Icon.
 *
 * Near-black #0A0A0A is the canvas.
 * White #F8FAFC is the text color, matching the arch.
 * Emerald #10B981 is the single accent, matching the coin.
 *
 * Text on emerald is ALWAYS #0A0A0A, never white (WCAG AAA 7.8:1).
 * Dynamic color is false to preserve strict brand integrity.
 */
private val DarkGateColorScheme: ColorScheme = darkColorScheme(
    primary = GateAccentEmerald,
    onPrimary = GateOnAccent, // #0A0A0A
    primaryContainer = GateAccentSubtleDark,
    onPrimaryContainer = GateAccentTextDark,
    secondary = GateAccentEmerald,
    onSecondary = GateOnAccent,
    secondaryContainer = GateSurface2Dark,
    onSecondaryContainer = GateTextDark,
    tertiary = GateWarningDark,
    onTertiary = GateNearBlack,
    tertiaryContainer = GateWarningDark.copy(alpha = 0.2f),
    onTertiaryContainer = GateWarningDark,
    background = GateBgDark,
    onBackground = GateTextDark,
    surface = GateSurfaceDark,
    onSurface = GateTextDark,
    surfaceVariant = GateSurfaceRaisedDark,
    onSurfaceVariant = GateTextMutedDark,
    surfaceTint = GateAccentEmerald,
    outline = GateBorderDark,
    outlineVariant = GateBorderStrongDark,
    error = GateDangerDark,
    onError = GateNearBlack,
    errorContainer = GateDangerDark.copy(alpha = 0.2f),
    onErrorContainer = GateDangerDark
)

private val LightGateColorScheme: ColorScheme = lightColorScheme(
    primary = GateAccentEmerald,
    onPrimary = GateOnAccent, // #0A0A0A on emerald
    primaryContainer = GateAccentSubtleLight,
    onPrimaryContainer = GateAccentTextLight,
    secondary = GateAccentTextLight,
    onSecondary = Color(0xFFFAFAFA),
    secondaryContainer = GateSurface2Light,
    onSecondaryContainer = GateTextLight,
    tertiary = GateWarningLight,
    onTertiary = Color.White,
    tertiaryContainer = GateWarningLight.copy(alpha = 0.15f),
    onTertiaryContainer = GateWarningLight,
    background = GateBgLight,
    onBackground = GateTextLight,
    surface = GateSurfaceLight,
    onSurface = GateTextLight,
    surfaceVariant = GateSurface2Light,
    onSurfaceVariant = GateTextMutedLight,
    surfaceTint = GateAccentEmerald,
    outline = GateBorderLight,
    outlineVariant = GateBorderStrongLight,
    error = GateDangerLight,
    onError = Color.White,
    errorContainer = GateDangerLight.copy(alpha = 0.12f),
    onErrorContainer = GateDangerLight
)

private val DarkGateColors = GateColors(
    bg = GateBgDark,
    surface = GateSurfaceDark,
    surfaceRaised = GateSurfaceRaisedDark,
    surface2 = GateSurface2Dark,
    border = GateBorderDark,
    borderStrong = GateBorderStrongDark,
    text = GateTextDark,
    textMuted = GateTextMutedDark,
    textSubtle = GateTextSubtleDark,
    accent = GateAccentDark,
    accentHover = GateAccentHoverDark,
    accentPressed = GateAccentPressedDark,
    onAccent = GateOnAccent,
    accentText = GateAccentTextDark,
    accentSubtle = GateAccentSubtleDark,
    success = GateSuccessDark,
    warning = GateWarningDark,
    danger = GateDangerDark
)

private val LightGateColors = GateColors(
    bg = GateBgLight,
    surface = GateSurfaceLight,
    surfaceRaised = GateSurfaceRaisedLight,
    surface2 = GateSurface2Light,
    border = GateBorderLight,
    borderStrong = GateBorderStrongLight,
    text = GateTextLight,
    textMuted = GateTextMutedLight,
    textSubtle = GateTextSubtleLight,
    accent = GateAccentLight,
    accentHover = GateAccentHoverLight,
    accentPressed = GateAccentPressedLight,
    onAccent = GateOnAccent,
    accentText = GateAccentTextLight,
    accentSubtle = GateAccentSubtleLight,
    success = GateSuccessLight,
    warning = GateWarningLight,
    danger = GateDangerLight
)

/**
 * Accessor for Gate UI custom semantic tokens in Composables:
 * e.g. GateTheme.colors.accent, GateTheme.colors.onAccent, GateTheme.colors.text
 */
object GateTheme {
    val colors: GateColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGateColors.current
}

/**
 * PayGateTheme wraps MaterialTheme with Gate UI icon-derived color scheme and typography.
 * Dynamic color is deliberately false to ensure uniform Gate UI branding.
 */
@Composable
fun PayGateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkGateColorScheme else LightGateColorScheme
    val gateColors = if (darkTheme) DarkGateColors else LightGateColors

    CompositionLocalProvider(
        LocalGateColors provides gateColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
