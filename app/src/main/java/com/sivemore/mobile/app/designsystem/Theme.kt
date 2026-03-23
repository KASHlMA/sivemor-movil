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
    primary = BrandGreen,
    onPrimary = Snow,
    primaryContainer = BrandGreenMuted,
    onPrimaryContainer = Snow,
    secondary = BrandGreenDeep,
    onSecondary = Snow,
    secondaryContainer = Background,
    onSecondaryContainer = Ink,
    tertiary = BrandMint,
    onTertiary = Snow,
    background = Background,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Background,
    onSurfaceVariant = MutedText,
    outline = Border,
)

private val SivemoreShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
