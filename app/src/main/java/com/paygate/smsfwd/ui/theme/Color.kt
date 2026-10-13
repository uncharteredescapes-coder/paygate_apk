package com.paygate.smsfwd.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * MFS Gateway Design System (DESIGN.md specification)
 * Derived straight from the Gate brand icon:
 *
 * 1. Near-black #0A0A0A is the canvas.
 * 2. White #F8FAFC is the text color, matching the arch.
 * 3. Emerald #10B981 is the single accent, matching the coin.
 *    Used only for the primary action, money, and success.
 *
 * Strict Rules:
 * - Text on emerald is ALWAYS #0A0A0A, never white (7.8:1 contrast AAA).
 * - There is no blue in the system. Informational states use neutral.
 * - One accent-filled element (primary action) per view. Everything else is neutral.
 * - Money is the hero: amounts and TrxIDs receive the clearest treatment.
 */

// ---------------------------------------------------------------------------
// 1. Neutral Palette (Pure gray, no blue tint)
// ---------------------------------------------------------------------------
val Neutral950 = Color(0xFF0A0A0A) // Dark canvas, text on emerald
val Neutral900 = Color(0xFF111111) // Dark surface
val Neutral850 = Color(0xFF171717) // Dark raised surface
val Neutral800 = Color(0xFF1F1F1F) // Dark hover / subtle fill
val Neutral700 = Color(0xFF262626) // Dark border
val Neutral600 = Color(0xFF333333) // Dark strong border
val Neutral500 = Color(0xFF8A8A8A) // Subtle text (dark theme)
val Neutral400 = Color(0xFFA3A3A3) // Muted text (dark theme)
val Neutral300 = Color(0xFFD4D4D4) // Light strong border
val Neutral200 = Color(0xFFE5E5E5) // Light border
val Neutral100 = Color(0xFFF0F0F0) // Light subtle fill
val Neutral50  = Color(0xFFFAFAFA) // Light canvas
val WhiteArch  = Color(0xFFF8FAFC) // Primary text on dark (matches the arch)

// ---------------------------------------------------------------------------
// 2. Emerald Palette (The Coin)
// ---------------------------------------------------------------------------
val Emerald50  = Color(0xFFECFDF5)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald300 = Color(0xFF6EE7B7)
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981) // Single brand accent
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857) // Accent text on light
val Emerald800 = Color(0xFF065F46)
val Emerald900 = Color(0xFF064E3B)
val Emerald950 = Color(0xFF022C22)

// ---------------------------------------------------------------------------
// 3. Status Tokens
// ---------------------------------------------------------------------------
val Amber500 = Color(0xFFF59E0B) // Pending / waiting (dark)
val Amber700 = Color(0xFFB45309) // Pending text on light
val Red500   = Color(0xFFEF4444) // Failed / destructive (dark)
val Red700   = Color(0xFFB91C1C) // Error text on light

// ---------------------------------------------------------------------------
// 4. Wallet Provider Marks (Restricted ONLY to indicator dots / chips)
// ---------------------------------------------------------------------------
val ProviderBkash  = Color(0xFFE2136E)
val ProviderNagad  = Color(0xFFF6921E)
val ProviderRocket = Color(0xFF8C3494)

// ---------------------------------------------------------------------------
// 5. Semantic Base Tokens
// ---------------------------------------------------------------------------
val GateNearBlack       = Neutral950 // #0A0A0A
val GateWhiteArch       = WhiteArch  // #F8FAFC
val GateAccentEmerald   = Emerald500 // #10B981
val GateOnAccent        = Neutral950 // #0A0A0A (text on emerald is always #0A0A0A)

// Dark Theme Defaults
val GateBgDark           = Neutral950 // #0A0A0A
val GateSurfaceDark      = Neutral900 // #111111
val GateSurfaceRaisedDark= Neutral850 // #171717
val GateSurface2Dark     = Neutral800 // #1F1F1F
val GateBorderDark       = Neutral700 // #262626
val GateBorderStrongDark = Neutral600 // #333333
val GateTextDark         = WhiteArch  // #F8FAFC
val GateTextMutedDark    = Neutral400 // #A3A3A3
val GateTextSubtleDark   = Neutral500 // #8A8A8A

val GateAccentDark       = Emerald500 // #10B981
val GateAccentHoverDark  = Emerald400 // #34D399
val GateAccentPressedDark= Emerald600 // #059669
val GateAccentTextDark   = Emerald400 // #34D399
val GateAccentSubtleDark = Emerald950 // #022C22

val GateSuccessDark      = Emerald500 // #10B981
val GateWarningDark      = Amber500   // #F59E0B
val GateDangerDark       = Red500     // #EF4444

// Light Theme Tokens
val GateBgLight          = Neutral50  // #FAFAFA
val GateSurfaceLight     = Color(0xFFFFFFFF)
val GateSurfaceRaisedLight=Color(0xFFFFFFFF)
val GateSurface2Light    = Neutral100 // #F0F0F0
val GateBorderLight      = Neutral200 // #E5E5E5
val GateBorderStrongLight= Neutral300 // #D4D4D4
val GateTextLight        = Neutral950 // #0A0A0A
val GateTextMutedLight   = Color(0xFF525252)
val GateTextSubtleLight  = Color(0xFF737373)

val GateAccentLight      = Emerald500 // #10B981
val GateAccentHoverLight = Emerald600 // #059669
val GateAccentPressedLight=Emerald700 // #047857
val GateAccentTextLight  = Emerald700 // #047857 (passes contrast on white)
val GateAccentSubtleLight= Emerald50  // #ECFDF5

val GateSuccessLight     = Emerald700 // #047857
val GateWarningLight     = Amber700   // #B45309
val GateDangerLight      = Red700     // #B91C1C

// ---------------------------------------------------------------------------
// 6. Gate UI Semantic Custom Color Tokens (Theme Extension)
// ---------------------------------------------------------------------------
@Immutable
data class GateColors(
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surface2: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val accent: Color,
    val accentHover: Color,
    val accentPressed: Color,
    val onAccent: Color,
    val accentText: Color,
    val accentSubtle: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    // Aliases ensuring seamless backward compatibility with existing components
    val ink: Color = text,
    val ink2: Color = textMuted,
    val ink3: Color = textSubtle,
    val accentSoft: Color = accentSubtle,
    val bkash: Color = ProviderBkash,
    val nagad: Color = ProviderNagad,
    val rocket: Color = ProviderRocket
)

val LocalGateColors = staticCompositionLocalOf {
    GateColors(
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
}

// ---------------------------------------------------------------------------
// 7. Backward Compatibility Status Aliases
// ---------------------------------------------------------------------------
val StatusSuccess = GateAccentEmerald // #10B981 (Emerald coin)
val StatusWarning = Amber500          // #F59E0B (Pending / waiting)
val StatusError   = Red500            // #EF4444 (Failed / destructive)
val StatusPending = Amber500          // #F59E0B (No blue in system, amber for pending)
val PayGateEmerald= GateAccentEmerald // #10B981
val PayGateBlue   = GateAccentEmerald // Purged blue, alias to Emerald
