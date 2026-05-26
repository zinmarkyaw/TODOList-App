package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

enum class AppThemeOption {
  LAVENDER, // Clean Minimalism Light
  SLATE,    // Clean Minimalism Dark (Slate)
  FOREST,   // Sage Forest Mint
  COZY      // Cozy Warm Amber
}

private val LightColorScheme =
  lightColorScheme(
    primary = CleanPrimary,
    onPrimary = CleanOnPrimary,
    primaryContainer = CleanPrimaryContainer,
    onPrimaryContainer = CleanOnPrimaryContainer,
    secondary = CleanSecondary,
    onSecondary = CleanOnSecondary,
    secondaryContainer = CleanSecondaryContainer,
    onSecondaryContainer = CleanOnSecondaryContainer,
    background = CleanBg,
    onBackground = CleanTextPrimary,
    surface = Color.White,
    onSurface = CleanTextPrimary,
    surfaceVariant = CleanSecondaryContainer,
    onSurfaceVariant = CleanSecondary,
    outline = CleanOutline,
    outlineVariant = CleanOutlineVariant,
    error = CleanError
  )

@Composable
fun MyApplicationTheme(
  themeOption: AppThemeOption = AppThemeOption.LAVENDER,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeOption) {
    AppThemeOption.LAVENDER -> LightColorScheme
    AppThemeOption.SLATE -> darkColorScheme(
      primary = SlatePrimary,
      onPrimary = SlateOnPrimary,
      primaryContainer = SlatePrimaryContainer,
      onPrimaryContainer = SlateOnPrimaryContainer,
      secondary = CleanSecondary,
      onSecondary = CleanOnSecondary,
      secondaryContainer = SlateSecondaryContainer,
      onSecondaryContainer = SlateOnPrimaryContainer,
      background = SlateBg,
      onBackground = SlateTextPrimary,
      surface = SlateBg,
      onSurface = SlateTextPrimary,
      surfaceVariant = SlateSecondaryContainer,
      onSurfaceVariant = SlateTextPrimary.copy(alpha = 0.7f),
      outline = CleanOutline,
      outlineVariant = SlateOutlineVariant,
      error = CleanError
    )
    AppThemeOption.FOREST -> lightColorScheme(
      primary = ForestPrimary,
      onPrimary = ForestOnPrimary,
      primaryContainer = ForestPrimaryContainer,
      onPrimaryContainer = ForestOnPrimaryContainer,
      secondary = CleanSecondary,
      onSecondary = CleanOnSecondary,
      secondaryContainer = ForestSecondaryContainer,
      onSecondaryContainer = ForestOnPrimaryContainer,
      background = ForestBg,
      onBackground = ForestTextPrimary,
      surface = Color.White,
      onSurface = ForestTextPrimary,
      surfaceVariant = ForestSecondaryContainer,
      onSurfaceVariant = ForestTextPrimary.copy(alpha = 0.7f),
      outline = CleanOutline,
      outlineVariant = CleanOutlineVariant,
      error = CleanError
    )
    AppThemeOption.COZY -> lightColorScheme(
      primary = CozyPrimary,
      onPrimary = CozyOnPrimary,
      primaryContainer = CozyPrimaryContainer,
      onPrimaryContainer = CozyOnPrimaryContainer,
      secondary = CleanSecondary,
      onSecondary = CleanOnSecondary,
      secondaryContainer = CozySecondaryContainer,
      onSecondaryContainer = CozyOnPrimaryContainer,
      background = CozyBg,
      onBackground = CozyTextPrimary,
      surface = Color.White,
      onSurface = CozyTextPrimary,
      surfaceVariant = CozySecondaryContainer,
      onSurfaceVariant = CozyTextPrimary.copy(alpha = 0.7f),
      outline = CleanOutline,
      outlineVariant = CleanOutlineVariant,
      error = CleanError
    )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
