package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkTechPrimary,
    onPrimary = DarkTechOnPrimary,
    primaryContainer = DarkTechPrimaryContainer,
    onPrimaryContainer = DarkTechOnPrimaryContainer,
    secondary = DarkTechSecondary,
    onSecondary = DarkTechOnSecondary,
    secondaryContainer = DarkTechSecondaryContainer,
    onSecondaryContainer = DarkTechOnSecondaryContainer,
    tertiary = DarkTechTertiary,
    onTertiary = DarkTechOnTertiary,
    tertiaryContainer = DarkTechTertiaryContainer,
    onTertiaryContainer = DarkTechOnTertiaryContainer,
    background = DarkTechBackground,
    surface = DarkTechSurface,
    surfaceVariant = DarkTechSurfaceVariant,
    onBackground = DarkTechOnBackground,
    onSurface = DarkTechOnSurface,
    onSurfaceVariant = DarkTechOnSurfaceVariant,
    outline = DarkTechOutline,
    outlineVariant = DarkTechOutlineVariant,
    error = TechError,
    onError = TechOnError,
    errorContainer = TechErrorContainer,
    onErrorContainer = TechOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = TechPrimary,
    onPrimary = TechOnPrimary,
    primaryContainer = TechPrimaryContainer,
    onPrimaryContainer = TechOnPrimaryContainer,
    secondary = TechSecondary,
    onSecondary = TechOnSecondary,
    secondaryContainer = TechSecondaryContainer,
    onSecondaryContainer = TechOnSecondaryContainer,
    tertiary = TechTertiary,
    onTertiary = TechOnTertiary,
    tertiaryContainer = TechTertiaryContainer,
    onTertiaryContainer = TechOnTertiaryContainer,
    background = TechBackground,
    surface = TechSurface,
    surfaceVariant = TechSurfaceVariant,
    onBackground = TechOnBackground,
    onSurface = TechOnSurface,
    onSurfaceVariant = TechOnSurfaceVariant,
    outline = TechOutline,
    outlineVariant = TechOutlineVariant,
    error = TechError,
    onError = TechOnError,
    errorContainer = TechErrorContainer,
    onErrorContainer = TechOnErrorContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false by default for a unified brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
