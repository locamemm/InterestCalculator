package com.example.interestcalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BankBluePrimaryDark,
    secondary = BankGreenPrimaryDark,
    tertiary = BankGoldAmber,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    primaryContainer = Color(0xFF0D2D59),
    secondaryContainer = Color(0xFF1B3B22)
)

private val LightColorScheme = lightColorScheme(
    primary = BankBluePrimary,
    onPrimary = Color.White,
    primaryContainer = BankBlueContainer,
    onPrimaryContainer = BankBluePrimary,
    secondary = BankGreenPrimary,
    onSecondary = Color.White,
    secondaryContainer = BankGreenContainer,
    onSecondaryContainer = BankGreenPrimary,
    tertiary = BankGoldAmber,
    tertiaryContainer = BankGoldContainer,
    background = BankBackground,
    surface = BankSurface,
    surfaceVariant = Color(0xFFF0F4F8),
    outline = Color(0xFFD0D7DE),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun InterestCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}