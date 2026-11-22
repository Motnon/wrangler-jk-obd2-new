package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WranglerDarkColorScheme = darkColorScheme(
    primary = OverlandAmber,
    onPrimary = ObsidianBg,
    primaryContainer = AmberDim,
    onPrimaryContainer = OverlandAmber,
    secondary = TrailSage,
    onSecondary = ObsidianBg,
    secondaryContainer = SageDim,
    onSecondaryContainer = TrailSage,
    tertiary = SteelBlue,
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

private val WranglerLightColorScheme = lightColorScheme(
    primary = OverlandAmberLight,
    onPrimary = DaylightSurface,
    primaryContainer = AmberContainerLight,
    onPrimaryContainer = OnAmberContainerLight,
    secondary = TrailSageLight,
    onSecondary = DaylightSurface,
    secondaryContainer = SageContainerLight,
    onSecondaryContainer = OnSageContainerLight,
    tertiary = SteelBlue,
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
    onError = DaylightSurface,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = OnCrimsonContainerLight
)

private val WranglerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun WranglerObdTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WranglerDarkColorScheme else WranglerLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = WranglerShapes,
        content = content
    )
}
