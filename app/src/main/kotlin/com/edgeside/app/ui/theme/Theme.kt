package com.edgeside.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 不依赖 Material 3 的 dynamicColor / 大圆角（用户明确不喜苹果风）
private val LightColors = lightColorScheme(
    primary = Blue700,
    onPrimary = OnSurfaceLight,
    primaryContainer = Blue200,
    onPrimaryContainer = Blue900,
    secondary = Blue500,
    onSecondary = OnSurfaceLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight,
    outline = OutlineLight,
    error = Crit,
    onError = OnSurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = Blue500,
    onPrimary = SurfaceDark,
    primaryContainer = Blue900,
    onPrimaryContainer = Blue200,
    secondary = Blue200,
    onSecondary = SurfaceDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark,
    outline = OutlineDark,
    error = Crit,
    onError = OnSurfaceDark
)

// 等宽字 + 紧凑字号，呼应 HUD/IDE 终端风格
private val MonoStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
)

private val EdgeSideTypography = Typography(
    bodyLarge = MonoStyle.copy(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = MonoStyle,
    bodySmall = MonoStyle.copy(fontSize = 11.sp, lineHeight = 16.sp),
    titleLarge = MonoStyle.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = MonoStyle.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    titleSmall = MonoStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelLarge = MonoStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
    labelMedium = MonoStyle.copy(fontSize = 11.sp),
    labelSmall = MonoStyle.copy(fontSize = 10.sp)
)

@Composable
fun EdgeSideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = EdgeSideTypography,
        content = content
    )
}
