package com.personal.biji.android.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.personal.biji.android.R

internal data class BijiSemanticColors(
    val brand: Color,
    val brandDeep: Color,
    val brandSoft: Color,
    val heroSurface: Color,
    val heroText: Color,
    val heroMuted: Color,
    val ink: Color,
    val inkMuted: Color,
    val canvas: Color,
    val surface: Color,
    val surfaceWarm: Color,
    val outlineSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
)

private val LightBijiColors = BijiSemanticColors(
    brand = Color(0xFF2ABB6A),
    brandDeep = Color(0xFF17834A),
    brandSoft = Color(0xFFE7F8EE),
    heroSurface = Color(0xFF18221C),
    heroText = Color.White,
    heroMuted = Color(0xFFCAD5CE),
    ink = Color(0xFF18221C),
    inkMuted = Color(0xFF68756E),
    canvas = Color(0xFFF7F8F4),
    surface = Color(0xFFFFFFFF),
    surfaceWarm = Color(0xFFF1F4EE),
    outlineSoft = Color(0xFFE1E7DF),
    danger = Color(0xFFC94B4B),
    dangerSoft = Color(0xFFFCECEC),
)

private val DarkBijiColors = BijiSemanticColors(
    brand = Color(0xFF47D982),
    brandDeep = Color(0xFF8DE6B4),
    brandSoft = Color(0xFF173B28),
    heroSurface = Color(0xFF1F2A22),
    heroText = Color(0xFFF5FAF6),
    heroMuted = Color(0xFFB9C8BF),
    ink = Color(0xFFE8EFEA),
    inkMuted = Color(0xFFA2B0A8),
    canvas = Color(0xFF101611),
    surface = Color(0xFF18211A),
    surfaceWarm = Color(0xFF202B23),
    outlineSoft = Color(0xFF334039),
    danger = Color(0xFFFF8A8A),
    dangerSoft = Color(0xFF3E2022),
)

private val LocalBijiColors = compositionLocalOf { LightBijiColors }

internal val Brand: Color @Composable get() = LocalBijiColors.current.brand
internal val BrandDeep: Color @Composable get() = LocalBijiColors.current.brandDeep
internal val BrandSoft: Color @Composable get() = LocalBijiColors.current.brandSoft
internal val HeroSurface: Color @Composable get() = LocalBijiColors.current.heroSurface
internal val HeroText: Color @Composable get() = LocalBijiColors.current.heroText
internal val HeroMuted: Color @Composable get() = LocalBijiColors.current.heroMuted
internal val Ink: Color @Composable get() = LocalBijiColors.current.ink
internal val InkMuted: Color @Composable get() = LocalBijiColors.current.inkMuted
internal val Canvas: Color @Composable get() = LocalBijiColors.current.canvas
internal val Surface: Color @Composable get() = LocalBijiColors.current.surface
internal val SurfaceWarm: Color @Composable get() = LocalBijiColors.current.surfaceWarm
internal val OutlineSoft: Color @Composable get() = LocalBijiColors.current.outlineSoft
internal val Danger: Color @Composable get() = LocalBijiColors.current.danger
internal val DangerSoft: Color @Composable get() = LocalBijiColors.current.dangerSoft

private fun bijiMaterialColors(colors: BijiSemanticColors, dark: Boolean) =
    if (dark) {
        darkColorScheme(
            primary = colors.brand,
            onPrimary = Color.White,
            primaryContainer = colors.brandSoft,
            onPrimaryContainer = colors.brandDeep,
            secondary = colors.brandDeep,
            background = colors.canvas,
            onBackground = colors.ink,
            surface = colors.surface,
            onSurface = colors.ink,
            surfaceVariant = colors.surfaceWarm,
            onSurfaceVariant = colors.inkMuted,
            outline = colors.outlineSoft,
            error = colors.danger,
            errorContainer = colors.dangerSoft,
        )
    } else {
        lightColorScheme(
            primary = colors.brand,
            onPrimary = Color.White,
            primaryContainer = colors.brandSoft,
            onPrimaryContainer = colors.brandDeep,
            secondary = colors.brandDeep,
            background = colors.canvas,
            onBackground = colors.ink,
            surface = colors.surface,
            onSurface = colors.ink,
            surfaceVariant = colors.surfaceWarm,
            onSurfaceVariant = colors.inkMuted,
            outline = colors.outlineSoft,
            error = colors.danger,
            errorContainer = colors.dangerSoft,
        )
    }

internal val LxgwWenKaiFontFamily = FontFamily(Font(R.font.lxgw_wenkai_gb_screen))

internal fun fontFamilyFor(preference: FontPreference): FontFamily =
    if (preference == FontPreference.LxgwWenKai) LxgwWenKaiFontFamily else FontFamily.Default

private fun typography(fontFamily: FontFamily): Typography {
    val defaults = Typography()
    fun TextStyle.withFont() = copy(fontFamily = fontFamily)
    return Typography(
        displayLarge = defaults.displayLarge.withFont(),
        displayMedium = defaults.displayMedium.withFont(),
        displaySmall = defaults.displaySmall.withFont(),
        headlineLarge = defaults.headlineLarge.withFont(),
        headlineMedium = defaults.headlineMedium.withFont(),
        headlineSmall = defaults.headlineSmall.withFont(),
        titleLarge = defaults.titleLarge.withFont(),
        titleMedium = defaults.titleMedium.withFont(),
        titleSmall = defaults.titleSmall.withFont(),
        bodyLarge = defaults.bodyLarge.withFont(),
        bodyMedium = defaults.bodyMedium.withFont(),
        bodySmall = defaults.bodySmall.withFont(),
        labelLarge = defaults.labelLarge.withFont(),
        labelMedium = defaults.labelMedium.withFont(),
        labelSmall = defaults.labelSmall.withFont(),
    )
}

@Composable
internal fun BijiTheme(fontPreference: FontPreference, themePreference: ThemePreference = ThemePreference.System, content: @Composable () -> Unit) {
    val dark = when (themePreference) {
        ThemePreference.System -> isSystemInDarkTheme()
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
    val colors = if (dark) DarkBijiColors else LightBijiColors
    val fontFamily = fontFamilyFor(fontPreference)
    MaterialTheme(
        colorScheme = bijiMaterialColors(colors, dark),
        typography = typography(fontFamily),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(30.dp),
        ),
    ) {
        CompositionLocalProvider(
            LocalBijiColors provides colors,
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = fontFamily),
        ) {
            content()
        }
    }
}
