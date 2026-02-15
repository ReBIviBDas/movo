package it.movo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MovoTeal,
    onPrimary = MovoWhite,
    primaryContainer = MovoTealContainer,
    onPrimaryContainer = OnMovoTealContainer,
    secondary = MovoTealDark,
    onSecondary = MovoWhite,
    secondaryContainer = MovoTealContainer,
    onSecondaryContainer = OnMovoTealContainer,
    tertiary = MovoTealLight,
    onTertiary = MovoWhite,
    background = MovoBackground,
    onBackground = MovoOnSurface,
    surface = MovoSurface,
    onSurface = MovoOnSurface,
    surfaceVariant = MovoSurfaceVariant,
    onSurfaceVariant = MovoOnSurfaceVariant,
    outline = MovoOutline,
    outlineVariant = MovoOutlineVariant,
    error = MovoError,
    onError = MovoWhite,
    errorContainer = MovoErrorContainer,
    onErrorContainer = OnMovoErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = MovoTealLight,
    onPrimary = OnMovoTealContainer,
    primaryContainer = MovoTealDark,
    onPrimaryContainer = MovoTealContainer,
    secondary = MovoTeal,
    onSecondary = OnMovoTealContainer,
    secondaryContainer = MovoTealDark,
    onSecondaryContainer = MovoTealContainer,
    tertiary = MovoTealLight,
    onTertiary = OnMovoTealContainer,
    background = MovoDarkBackground,
    onBackground = MovoDarkOnSurface,
    surface = MovoDarkSurface,
    onSurface = MovoDarkOnSurface,
    surfaceVariant = MovoDarkSurfaceVariant,
    onSurfaceVariant = MovoDarkOnSurfaceVariant,
    outline = MovoDarkOutline,
    outlineVariant = MovoDarkOutline,
    error = MovoError,
    onError = MovoWhite,
    errorContainer = MovoErrorContainer,
    onErrorContainer = OnMovoErrorContainer
)

@Composable
fun MovoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MovoTypography,
        content = content
    )
}
