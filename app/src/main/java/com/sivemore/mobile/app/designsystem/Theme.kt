package com.sivemore.mobile.app.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Clay,
    onPrimary = Snow,
    primaryContainer = RoseDust,
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Snow,
    secondaryContainer = Mist,
    onSecondaryContainer = Ink,
    tertiary = Sun,
    onTertiary = Ink,
    background = Shell,
    onBackground = Ink,
    surface = Snow,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Slate,
    outline = Sand,
)

private val SivemoreShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

@Immutable
data class SivemoreElevations(
    val card: androidx.compose.ui.unit.Dp = 1.dp,
)

private val LocalElevations = staticCompositionLocalOf { SivemoreElevations() }

@Composable
fun SivemoreTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpacing provides SivemoreSpacing(),
        LocalElevations provides SivemoreElevations(),
    ) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = SivemoreTypography,
            shapes = SivemoreShapes,
            content = content,
        )
    }
}

object SivemoreThemeTokens {
    val spacing: SivemoreSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val elevations: SivemoreElevations
        @Composable
        @ReadOnlyComposable
        get() = LocalElevations.current
}
