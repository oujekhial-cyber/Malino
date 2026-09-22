package ir.kharjyar.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
        displayLarge = base.displayLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        displayMedium = base.displayMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        displaySmall = base.displaySmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = Vazirmatn, lineHeight = 28.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = Vazirmatn, lineHeight = 24.sp),
        bodySmall = base.bodySmall.copy(fontFamily = Vazirmatn),
        labelLarge = base.labelLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = Vazirmatn),
        labelSmall = base.labelSmall.copy(fontFamily = Vazirmatn)
    )
}

/**
 * مشخصات بصری هر تم: گرادیان‌ها، شفافیت کارت‌ها، حاشیه‌ها و رنگ نمودار.
 * این مقادیر با CompositionLocal در کل برنامه (نمودار، دیالوگ، کارت‌ها) در دسترس‌اند.
 */
data class AppSkin(
    val id: Palette,
    val title: String,
    val dark: Boolean,
    /** رنگ‌های پس‌زمینه صفحه (گرادیان عمودی/مورب). */
    val backgroundColors: List<Color>,
    /** لکه‌های نوری/موج‌های پس‌زمینه: (رنگ، شعاع نسبی، مرکز نسبی x, y). */
    val backdropBlobs: List<Blob>,
    val cardColor: Color,
    val cardAlpha: Float,
    val cardBorderColors: List<Color>,
    val cardBorderWidth: Dp,
    val cardCorner: Dp,
    val heroGradient: List<Color>,
    val fabGradient: List<Color>,
    val accent: Color,
    val incomeColor: Color,
    val expenseColor: Color,
    /** درخشش (glow) زیر خطوط نمودار. */
    val chartGlow: Boolean,
    val bigNumberColor: Color,
    val onBackdrop: Color,
    val dialogColor: Color
) {
    data class Blob(val color: Color, val radius: Float, val cx: Float, val cy: Float)
}

private val AppShapesGlass = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private fun shapesFor(corner: Dp) = Shapes(
    extraSmall = RoundedCornerShape(corner / 2.4f),
    small = RoundedCornerShape(corner / 1.8f),
    medium = RoundedCornerShape(corner / 1.3f),
    large = RoundedCornerShape(corner),
    extraLarge = RoundedCornerShape(corner + 4.dp)
)

// ---------------------------------------------------------------- تم ۱: شیشه‌ای
private val GlassScheme = darkColorScheme(
    primary = Color(0xFF8AA2FF), onPrimary = Color(0xFF0B1240),
    primaryContainer = Color(0xFF2B3A8F), onPrimaryContainer = Color(0xFFE3E7FF),
    secondary = Color(0xFFC0A8FF), onSecondary = Color(0xFF231250),
    secondaryContainer = Color(0xFF453089), onSecondaryContainer = Color(0xFFEDE4FF),
    tertiary = Color(0xFF7BE0E8), onTertiary = Color(0xFF04303A),
    tertiaryContainer = Color(0xFF1D4E5A), onTertiaryContainer = Color(0xFFCFF7FB),
    background = Color(0xFF0E1235), onBackground = Color(0xFFECEEFF),
    surface = Color(0xFF181C4A), onSurface = Color(0xFFECEEFF),
    surfaceVariant = Color(0xFF2A2F63), onSurfaceVariant = Color(0xFFC6C9EE),
    outline = Color(0x66FFFFFF), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF9EA8), onError = Color(0xFF4A0710),
    errorContainer = Color(0xFF6D1421), onErrorContainer = Color(0xFFFFDADE)
)

private val GlassSkin = AppSkin(
    id = Palette.GLASS,
    title = "شیشه‌ای",
    dark = true,
    backgroundColors = listOf(Color(0xFF0B1038), Color(0xFF181447), Color(0xFF2A1152)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x553D5AFE), 0.75f, 0.15f, 0.12f),
        AppSkin.Blob(Color(0x4D9C27FF), 0.85f, 0.9f, 0.35f),
        AppSkin.Blob(Color(0x3300E5FF), 0.6f, 0.5f, 0.85f)
    ),
    cardColor = Color(0xFF2B2F72),
    cardAlpha = 0.55f,
    cardBorderColors = listOf(Color(0x40FFFFFF), Color(0x40FFFFFF)),
    cardBorderWidth = 1.dp,
    cardCorner = 24.dp,
    heroGradient = listOf(Color(0xFF3D5AFE), Color(0xFF9C27FF)),
    fabGradient = listOf(Color(0xFF3D5AFE), Color(0xFF9C27FF)),
    accent = Color(0xFF8AA2FF),
    incomeColor = Color(0xFF6BE9C0),
    expenseColor = Color(0xFFFF8FA8),
    chartGlow = false,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFECEEFF),
    dialogColor = Color(0xFF1C2059)
)

// ---------------------------------------------------------------- تم ۲: نئون تاریک
private val NeonScheme = darkColorScheme(
    primary = Color(0xFF00E5FF), onPrimary = Color(0xFF00232B),
    primaryContainer = Color(0xFF00363F), onPrimaryContainer = Color(0xFFB4F6FF),
    secondary = Color(0xFFFF2ED2), onSecondary = Color(0xFF3A0030),
    secondaryContainer = Color(0xFF4F0442), onSecondaryContainer = Color(0xFFFFD6F4),
    tertiary = Color(0xFF9B7BFF), onTertiary = Color(0xFF1C0A4A),
    tertiaryContainer = Color(0xFF2C1470), onTertiaryContainer = Color(0xFFE4DAFF),
    background = Color(0xFF000000), onBackground = Color(0xFFE9F7FA),
    surface = Color(0xFF121212), onSurface = Color(0xFFE9F7FA),
    surfaceVariant = Color(0xFF1C1C1C), onSurfaceVariant = Color(0xFFB4BFC2),
    outline = Color(0xFF00E5FF), outlineVariant = Color(0xFF2A2A2A),
    error = Color(0xFFFF5C8A), onError = Color(0xFF3A0013),
    errorContainer = Color(0xFF52001C), onErrorContainer = Color(0xFFFFD9E2)
)

private val NeonSkin = AppSkin(
    id = Palette.NEON,
    title = "نئون تاریک",
    dark = true,
    backgroundColors = listOf(Color(0xFF000000), Color(0xFF000000)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x2200E5FF), 0.7f, 0.1f, 0.08f),
        AppSkin.Blob(Color(0x22FF2ED2), 0.7f, 0.95f, 0.5f)
    ),
    cardColor = Color(0xFF121212),
    cardAlpha = 1f,
    cardBorderColors = listOf(Color(0xFF00E5FF), Color(0xFFFF2ED2)),
    cardBorderWidth = 1.5.dp,
    cardCorner = 20.dp,
    heroGradient = listOf(Color(0xFF041A20), Color(0xFF1B0420)),
    fabGradient = listOf(Color(0xFF00E5FF), Color(0xFFFF2ED2)),
    accent = Color(0xFF00E5FF),
    incomeColor = Color(0xFF00E5FF),
    expenseColor = Color(0xFFFF2ED2),
    chartGlow = true,
    bigNumberColor = Color(0xFF00E5FF),
    onBackdrop = Color(0xFFE9F7FA),
    dialogColor = Color(0xFF121212)
)

// ---------------------------------------------------------------- تم ۳: پاستلی روشن
private val PastelScheme = lightColorScheme(
    primary = Color(0xFF7A5CC4), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEFF), onPrimaryContainer = Color(0xFF261356),
    secondary = Color(0xFFE08A6A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0D3), onSecondaryContainer = Color(0xFF3A1508),
    tertiary = Color(0xFF2E9E6B), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB9F0C9), onTertiaryContainer = Color(0xFF00301A),
    background = Color(0xFFFFF8F2), onBackground = Color(0xFF2B2430),
    surface = Color(0xFFFFFDFB), onSurface = Color(0xFF2B2430),
    surfaceVariant = Color(0xFFF2E7E0), onSurfaceVariant = Color(0xFF5B4F52),
    outline = Color(0xFFCFC0BB), outlineVariant = Color(0xFFE6D9D3),
    error = Color(0xFFD1564B), onError = Color.White,
    errorContainer = Color(0xFFFFC2B3), onErrorContainer = Color(0xFF40100A)
)

private val PastelSkin = AppSkin(
    id = Palette.PASTEL,
    title = "پاستلی روشن",
    dark = false,
    backgroundColors = listOf(Color(0xFFFFF8F2), Color(0xFFFFF8F2)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x33FFBFA3), 0.7f, 0.12f, 0.1f),
        AppSkin.Blob(Color(0x33C9B6F8), 0.7f, 0.92f, 0.4f)
    ),
    cardColor = Color(0xFFFFFFFF),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x33C9B6F8), Color(0x33FFBFA3)),
    cardBorderWidth = 1.dp,
    cardCorner = 28.dp,
    heroGradient = listOf(Color(0xFFFFBFA3), Color(0xFFC9B6F8)),
    fabGradient = listOf(Color(0xFFFFBFA3), Color(0xFFC9B6F8)),
    accent = Color(0xFF7A5CC4),
    incomeColor = Color(0xFF3FBE83),
    expenseColor = Color(0xFFEE7B62),
    chartGlow = false,
    bigNumberColor = Color(0xFF2B2430),
    onBackdrop = Color(0xFF2B2430),
    dialogColor = Color(0xFFFFFDFB)
)

/** چیپ‌های مخصوص تم پاستلی (درآمد نعنایی / هزینه مرجانی). */
val PastelIncomeChip = Color(0xFFB9F0C9)
val PastelExpenseChip = Color(0xFFFFC2B3)

// ---------------------------------------------------------------- تم ۴: شفق قطبی
private val AuroraScheme = darkColorScheme(
    primary = Color(0xFF2BE4A8), onPrimary = Color(0xFF00301F),
    primaryContainer = Color(0xFF00513A), onPrimaryContainer = Color(0xFFB8FFE2),
    secondary = Color(0xFF7C5CFF), onSecondary = Color(0xFF16053F),
    secondaryContainer = Color(0xFF34217A), onSecondaryContainer = Color(0xFFE5DCFF),
    tertiary = Color(0xFF6FD7F5), onTertiary = Color(0xFF00323F),
    tertiaryContainer = Color(0xFF064A5C), onTertiaryContainer = Color(0xFFC6F1FF),
    background = Color(0xFF0B2530), onBackground = Color(0xFFE2F4F1),
    surface = Color(0xFF103241), onSurface = Color(0xFFE2F4F1),
    surfaceVariant = Color(0xFF1A4152), onSurfaceVariant = Color(0xFFB6D2D8),
    outline = Color(0x662BE4A8), outlineVariant = Color(0x332BE4A8),
    error = Color(0xFFFF8E9E), onError = Color(0xFF41000C),
    errorContainer = Color(0xFF63102A), onErrorContainer = Color(0xFFFFD9DE)
)

private val AuroraSkin = AppSkin(
    id = Palette.AURORA,
    title = "شفق قطبی",
    dark = true,
    backgroundColors = listOf(Color(0xFF0B2530), Color(0xFF0B2530)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x552BE4A8), 0.9f, 0.1f, 0.12f),
        AppSkin.Blob(Color(0x4D7C5CFF), 0.95f, 0.95f, 0.45f),
        AppSkin.Blob(Color(0x332BE4A8), 0.7f, 0.5f, 0.95f)
    ),
    cardColor = Color(0xFF12394A),
    cardAlpha = 0.62f,
    cardBorderColors = listOf(Color(0xFF2BE4A8), Color(0xFF7C5CFF)),
    cardBorderWidth = 1.2.dp,
    cardCorner = 24.dp,
    heroGradient = listOf(Color(0xFF12594B), Color(0xFF2E2470)),
    fabGradient = listOf(Color(0xFF2BE4A8), Color(0xFF7C5CFF)),
    accent = Color(0xFF2BE4A8),
    incomeColor = Color(0xFF2BE4A8),
    expenseColor = Color(0xFFFF8E9E),
    chartGlow = true,
    bigNumberColor = Color(0xFF9CFFDF),
    onBackdrop = Color(0xFFE2F4F1),
    dialogColor = Color(0xFF103241)
)

/** همه تم‌های موجود، به ترتیب نمایش در تنظیمات. */
val AllSkins: List<AppSkin> = listOf(GlassSkin, NeonSkin, PastelSkin, AuroraSkin)

fun skinOf(palette: Palette): AppSkin = when (palette) {
    Palette.GLASS -> GlassSkin
    Palette.NEON -> NeonSkin
    Palette.PASTEL -> PastelSkin
    Palette.AURORA -> AuroraSkin
}

val LocalAppSkin: ProvidableCompositionLocal<AppSkin> = compositionLocalOf { GlassSkin }

@Suppress("UNUSED_PARAMETER")
@Composable
fun KharjYarTheme(
    themeMode: ThemeMode,
    palette: Palette,
    content: @Composable () -> Unit
) {
    // تم‌ها خودشان روشن/تاریک را تعریف می‌کنند؛ ThemeMode فقط برای سازگاری نگه داشته شده است.
    val skin = skinOf(palette)
    val colorScheme = when (palette) {
        Palette.GLASS -> GlassScheme
        Palette.NEON -> NeonScheme
        Palette.PASTEL -> PastelScheme
        Palette.AURORA -> AuroraScheme
    }
    CompositionLocalProvider(LocalAppSkin provides skin) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography(),
            shapes = if (palette == Palette.GLASS) AppShapesGlass else shapesFor(skin.cardCorner),
            content = content
        )
    }
}
