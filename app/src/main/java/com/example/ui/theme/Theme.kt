package com.example.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.obd.AppUiStyle
import com.example.obd.AppVisualTheme

@Immutable
data class RaceClusterPalette(
    val boardBg: Color,
    val surface: Color,
    val podSurface: Color,
    val bezelBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val zone1: Color,
    val zone2: Color,
    val zone3: Color,
    val zone4: Color,
    val zone5: Color,
    val accentBlue: Color,
    val accentViolet: Color,
    val isNightMode: Boolean
)

val NeonCyanAmberRacePalette = RaceClusterPalette(
    boardBg = RaceCarbonBg,
    surface = RaceClusterSurface,
    podSurface = RacePodSurface,
    bezelBorder = RaceBezelBorder,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    zone1 = RaceCyan,
    zone2 = RaceGreen,
    zone3 = RaceYellow,
    zone4 = RaceOrange,
    zone5 = RaceRed,
    accentBlue = RaceBlue,
    accentViolet = RaceViolet,
    isNightMode = false
)

val RacingRedRacePalette = RaceClusterPalette(
    boardBg = RacingRedBg,
    surface = RacingRedSurface,
    podSurface = RacingRedCard,
    bezelBorder = RacingRedBorder,
    textPrimary = TextPrimaryDark,
    textSecondary = RacingSilver,
    zone1 = RacingSilver,
    zone2 = RacingHotOrange,
    zone3 = RaceYellow,
    zone4 = RacingRedPrimary,
    zone5 = RaceRed,
    accentBlue = RacingHotOrange,
    accentViolet = RacingRedPrimary,
    isNightMode = false
)

val DeepGreenRacePalette = RaceClusterPalette(
    boardBg = DeepGreenBg,
    surface = DeepGreenSurface,
    podSurface = DeepGreenCard,
    bezelBorder = DeepGreenBorder,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    zone1 = DeepGreenPrimary,
    zone2 = DeepGreenLime,
    zone3 = DeepGreenGold,
    zone4 = OverlandAmber,
    zone5 = FaultCrimson,
    accentBlue = TrailSage,
    accentViolet = DeepGreenLime,
    isNightMode = false
)

val ClassicWhiteRacePalette = RaceClusterPalette(
    boardBg = DaylightBg,
    surface = DaylightSurface,
    podSurface = DaylightCard,
    bezelBorder = DaylightBorder,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    zone1 = ClassicBlueAccent,
    zone2 = TrailSageLight,
    zone3 = ClassicGoldAccent,
    zone4 = OverlandAmberLight,
    zone5 = FaultCrimsonLight,
    accentBlue = ClassicTealAccent,
    accentViolet = ClassicVioletAccent,
    isNightMode = false
)

val NightVisionRacePalette = RaceClusterPalette(
    boardBg = NightBg,
    surface = NightSurface,
    podSurface = NightCard,
    bezelBorder = NightBorder,
    textPrimary = NightTextPrimary,
    textSecondary = NightTextSecondary,
    zone1 = NightRedMuted,
    zone2 = NightRedSecondary,
    zone3 = NightRedPrimary,
    zone4 = NightRedPrimary,
    zone5 = FaultCrimson,
    accentBlue = NightRedSecondary,
    accentViolet = NightRedPrimary,
    isNightMode = true
)

val LocalRacePalette = staticCompositionLocalOf { NeonCyanAmberRacePalette }

private val NeonCyanAmberColorScheme = darkColorScheme(
    primary = OverlandAmber,
    onPrimary = ObsidianBg,
    primaryContainer = AmberDim,
    onPrimaryContainer = OverlandAmber,
    secondary = TrailSage,
    onSecondary = ObsidianBg,
    secondaryContainer = SageDim,
    onSecondaryContainer = TrailSage,
    tertiary = RaceCyan,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = CockpitSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = CockpitSurface,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = ElevatedSurface,
    outline = TacticalBorder,
    outlineVariant = TacticalBorder,
    error = FaultCrimson,
    onError = TextPrimaryDark,
    errorContainer = CrimsonDim,
    onErrorContainer = FaultCrimson
)

private val RacingRedColorScheme = darkColorScheme(
    primary = RacingRedPrimary,
    onPrimary = TextPrimaryDark,
    primaryContainer = RacingRedDim,
    onPrimaryContainer = RacingRedPrimary,
    secondary = RacingHotOrange,
    onSecondary = RacingRedBg,
    secondaryContainer = AmberDim,
    onSecondaryContainer = RacingHotOrange,
    tertiary = RacingSilver,
    onTertiary = RacingRedBg,
    background = RacingRedBg,
    onBackground = TextPrimaryDark,
    surface = RacingRedSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = RacingRedCard,
    onSurfaceVariant = RacingSilver,
    surfaceContainerLow = RacingRedSurface,
    surfaceContainer = RacingRedCard,
    surfaceContainerHigh = RacingRedCard,
    outline = RacingRedBorder,
    outlineVariant = RacingRedBorder,
    error = FaultCrimson,
    onError = TextPrimaryDark,
    errorContainer = CrimsonDim,
    onErrorContainer = FaultCrimson
)

private val DeepGreenColorScheme = darkColorScheme(
    primary = DeepGreenPrimary,
    onPrimary = DeepGreenBg,
    primaryContainer = DeepGreenDim,
    onPrimaryContainer = DeepGreenPrimary,
    secondary = DeepGreenLime,
    onSecondary = DeepGreenBg,
    secondaryContainer = SageDim,
    onSecondaryContainer = DeepGreenLime,
    tertiary = DeepGreenGold,
    onTertiary = DeepGreenBg,
    background = DeepGreenBg,
    onBackground = TextPrimaryDark,
    surface = DeepGreenSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DeepGreenCard,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = DeepGreenSurface,
    surfaceContainer = DeepGreenCard,
    surfaceContainerHigh = DeepGreenCard,
    outline = DeepGreenBorder,
    outlineVariant = DeepGreenBorder,
    error = FaultCrimson,
    onError = TextPrimaryDark,
    errorContainer = CrimsonDim,
    onErrorContainer = FaultCrimson
)

private val ClassicWhiteColorScheme = lightColorScheme(
    primary = ClassicBlueAccent,
    onPrimary = Color(0xFFF8FAFC),
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = OnBlueContainerLight,
    secondary = ClassicAmberAccent,
    onSecondary = Color(0xFFF8FAFC),
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = OnAmberContainerLight,
    tertiary = TrailSageLight,
    onTertiary = Color(0xFFF8FAFC),
    tertiaryContainer = SageContainerLight,
    onTertiaryContainer = OnSageContainerLight,
    background = DaylightBg,
    onBackground = TextPrimaryLight,
    surface = DaylightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DaylightCard,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = DaylightSurface,
    surfaceContainer = DaylightCard,
    surfaceContainerHigh = DaylightCard,
    outline = DaylightBorder,
    outlineVariant = DaylightBorder,
    error = FaultCrimsonLight,
    onError = Color(0xFFF8FAFC),
    errorContainer = CrimsonContainerLight,
    onErrorContainer = OnCrimsonContainerLight
)

private val NightVisionColorScheme = darkColorScheme(
    primary = NightRedPrimary,
    onPrimary = NightBg,
    primaryContainer = NightRedDim,
    onPrimaryContainer = NightTextPrimary,
    secondary = NightRedSecondary,
    onSecondary = NightBg,
    secondaryContainer = NightCard,
    onSecondaryContainer = NightTextPrimary,
    tertiary = NightRedMuted,
    onTertiary = NightTextPrimary,
    background = NightBg,
    onBackground = NightTextPrimary,
    surface = NightSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightCard,
    onSurfaceVariant = NightTextSecondary,
    surfaceContainerLow = NightSurface,
    surfaceContainer = NightCard,
    surfaceContainerHigh = NightElevated,
    outline = NightBorder,
    outlineVariant = NightBorder,
    error = NightRedPrimary,
    onError = NightBg,
    errorContainer = NightRedDim,
    onErrorContainer = NightTextPrimary
)

private val WranglerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val AvionicsHudShapes = Shapes(
    extraSmall = CutCornerShape(4.dp),
    small = CutCornerShape(7.dp),
    medium = CutCornerShape(10.dp),
    large = CutCornerShape(14.dp),
    extraLarge = CutCornerShape(18.dp)
)

private val AutomotiveSplitShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun WranglerObdTheme(
    visualTheme: AppVisualTheme = AppVisualTheme.NEON_CYAN_AMBER,
    isNightMode: Boolean = false,
    uiStyle: AppUiStyle = AppUiStyle.TACTICAL_MOTORSPORT,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isNightMode) {
        NightVisionColorScheme
    } else {
        when (visualTheme) {
            AppVisualTheme.NEON_CYAN_AMBER -> NeonCyanAmberColorScheme
            AppVisualTheme.RACING_RED -> RacingRedColorScheme
            AppVisualTheme.DEEP_GREEN -> DeepGreenColorScheme
            AppVisualTheme.CLASSIC_WHITE -> ClassicWhiteColorScheme
        }
    }

    val racePalette = if (isNightMode) {
        NightVisionRacePalette
    } else {
        when (visualTheme) {
            AppVisualTheme.NEON_CYAN_AMBER -> NeonCyanAmberRacePalette
            AppVisualTheme.RACING_RED -> RacingRedRacePalette
            AppVisualTheme.DEEP_GREEN -> DeepGreenRacePalette
            AppVisualTheme.CLASSIC_WHITE -> ClassicWhiteRacePalette
        }
    }

    val shapes = when (uiStyle) {
        AppUiStyle.TACTICAL_MOTORSPORT -> WranglerShapes
        AppUiStyle.AVIONICS_HUD -> AvionicsHudShapes
        AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> AutomotiveSplitShapes
    }

    CompositionLocalProvider(LocalRacePalette provides racePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = shapes,
            content = content
        )
    }
}
