package ir.kharjyar.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.R
import ir.kharjyar.app.data.prefs.Palette
import ir.kharjyar.app.data.prefs.ThemeMode

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private fun typography() = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Vazirmatn),
        displayMedium = base.displayMedium.copy(fontFamily = Vazirmatn),
        displaySmall = base.displaySmall.copy(fontFamily = Vazirmatn),
        headlineLarge = base.headlineLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = Vazirmatn, lineHeight = 28.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = Vazirmatn, lineHeight = 24.sp),
        bodySmall = base.bodySmall.copy(fontFamily = Vazirmatn),
        labelLarge = base.labelLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = Vazirmatn),
        labelSmall = base.labelSmall.copy(fontFamily = Vazirmatn)
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ---------- پالت‌های دستی ----------

private val OceanLight = lightColorScheme(
    primary = Color(0xFF00658E), onPrimary = Color.White,
    primaryContainer = Color(0xFFC7E7FF), onPrimaryContainer = Color(0xFF001E2E),
    secondary = Color(0xFF4F616E), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E5F5), onSecondaryContainer = Color(0xFF0B1D29),
    tertiary = Color(0xFF006874), onTertiary = Color.White,
    tertiaryContainer = Color(0xFF97F0FF), onTertiaryContainer = Color(0xFF001F24),
    background = Color(0xFFF6FAFE), onBackground = Color(0xFF181C20),
    surface = Color(0xFFF6FAFE), onSurface = Color(0xFF181C20),
    surfaceVariant = Color(0xFFDDE3EA), onSurfaceVariant = Color(0xFF41474D),
    error = Color(0xFFBA1A1A), onError = Color.White
)
private val OceanDark = darkColorScheme(
    primary = Color(0xFF85CFFF), onPrimary = Color(0xFF00344C),
    primaryContainer = Color(0xFF004C6C), onPrimaryContainer = Color(0xFFC7E7FF),
    secondary = Color(0xFFB6C9D8), onSecondary = Color(0xFF21323E),
    secondaryContainer = Color(0xFF384956), onSecondaryContainer = Color(0xFFD2E5F5),
    tertiary = Color(0xFF4FD8EB), onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F58), onTertiaryContainer = Color(0xFF97F0FF),
    background = Color(0xFF0F1417), onBackground = Color(0xFFDFE3E7),
    surface = Color(0xFF0F1417), onSurface = Color(0xFFDFE3E7),
    surfaceVariant = Color(0xFF41474D), onSurfaceVariant = Color(0xFFC1C7CE),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

private val ForestLight = lightColorScheme(
    primary = Color(0xFF2E6B27), onPrimary = Color.White,
    primaryContainer = Color(0xFFB0F49F), onPrimaryContainer = Color(0xFF002200),
    secondary = Color(0xFF54624D), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E8CD), onSecondaryContainer = Color(0xFF121F0E),
    tertiary = Color(0xFF386568), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCEBEE), onTertiaryContainer = Color(0xFF002022),
    background = Color(0xFFF8FBF1), onBackground = Color(0xFF191D16),
    surface = Color(0xFFF8FBF1), onSurface = Color(0xFF191D16),
    surfaceVariant = Color(0xFFDFE4D7), onSurfaceVariant = Color(0xFF43483E),
    error = Color(0xFFBA1A1A), onError = Color.White
)
private val ForestDark = darkColorScheme(
    primary = Color(0xFF95D785), onPrimary = Color(0xFF003A00),
    primaryContainer = Color(0xFF14520F), onPrimaryContainer = Color(0xFFB0F49F),
    secondary = Color(0xFFBBCBB1), onSecondary = Color(0xFF263422),
    secondaryContainer = Color(0xFF3C4B37), onSecondaryContainer = Color(0xFFD7E8CD),
    tertiary = Color(0xFFA0CFD2), onTertiary = Color(0xFF003739),
    tertiaryContainer = Color(0xFF1E4D50), onTertiaryContainer = Color(0xFFBCEBEE),
    background = Color(0xFF11150E), onBackground = Color(0xFFE1E4DA),
    surface = Color(0xFF11150E), onSurface = Color(0xFFE1E4DA),
    surfaceVariant = Color(0xFF43483E), onSurfaceVariant = Color(0xFFC3C8BB),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

private val SunsetLight = lightColorScheme(
    primary = Color(0xFFA63D00), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCB), onPrimaryContainer = Color(0xFF370E00),
    secondary = Color(0xFF77574A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCB), onSecondaryContainer = Color(0xFF2C160B),
    tertiary = Color(0xFF695E30), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1E2A8), onTertiaryContainer = Color(0xFF211B00),
    background = Color(0xFFFFF8F5), onBackground = Color(0xFF221A15),
    surface = Color(0xFFFFF8F5), onSurface = Color(0xFF221A15),
    surfaceVariant = Color(0xFFF4DED4), onSurfaceVariant = Color(0xFF52443C),
    error = Color(0xFFBA1A1A), onError = Color.White
)
private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFB596), onPrimary = Color(0xFF591D00),
    primaryContainer = Color(0xFF7F2C00), onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFFE7BEAD), onSecondary = Color(0xFF442A1F),
    secondaryContainer = Color(0xFF5D4034), onSecondaryContainer = Color(0xFFFFDBCB),
    tertiary = Color(0xFFD4C68E), onTertiary = Color(0xFF383006),
    tertiaryContainer = Color(0xFF50471B), onTertiaryContainer = Color(0xFFF1E2A8),
    background = Color(0xFF1A120D), onBackground = Color(0xFFF0DFD7),
    surface = Color(0xFF1A120D), onSurface = Color(0xFFF0DFD7),
    surfaceVariant = Color(0xFF52443C), onSurfaceVariant = Color(0xFFD7C3B8),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

private val MidnightLight = lightColorScheme(
    primary = Color(0xFF4C5BB3), onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE0FF), onPrimaryContainer = Color(0xFF000E5E),
    secondary = Color(0xFF5B5D72), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E1F9), onSecondaryContainer = Color(0xFF181A2C),
    tertiary = Color(0xFF77536D), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD7F1), onTertiaryContainer = Color(0xFF2D1228),
    background = Color(0xFFFBF8FF), onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF), onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE3E1EC), onSurfaceVariant = Color(0xFF46464F),
    error = Color(0xFFBA1A1A), onError = Color.White
)
private val MidnightDark = darkColorScheme(
    primary = Color(0xFFBBC3FF), onPrimary = Color(0xFF17277D),
    primaryContainer = Color(0xFF334199), onPrimaryContainer = Color(0xFFDFE0FF),
    secondary = Color(0xFFC4C5DD), onSecondary = Color(0xFF2D2F42),
    secondaryContainer = Color(0xFF434559), onSecondaryContainer = Color(0xFFE0E1F9),
    tertiary = Color(0xFFE6BAD7), onTertiary = Color(0xFF44263E),
    tertiaryContainer = Color(0xFF5D3C55), onTertiaryContainer = Color(0xFFFFD7F1),
    background = Color(0xFF0B0E1A), onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF0B0E1A), onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF46464F), onSurfaceVariant = Color(0xFFC7C5D0),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)

@Composable
fun KharjYarTheme(
    themeMode: ThemeMode,
    palette: Palette,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = when (palette) {
        Palette.DYNAMIC ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (dark) OceanDark else OceanLight
            }
        Palette.OCEAN -> if (dark) OceanDark else OceanLight
        Palette.FOREST -> if (dark) ForestDark else ForestLight
        Palette.SUNSET -> if (dark) SunsetDark else SunsetLight
        Palette.MIDNIGHT -> if (dark) MidnightDark else MidnightLight
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography(),
        shapes = AppShapes,
        content = content
    )
}
