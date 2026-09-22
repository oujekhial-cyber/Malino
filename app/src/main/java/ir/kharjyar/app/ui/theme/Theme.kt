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

private fun shapesFor(corner: Dp) = Shapes(
    extraSmall = RoundedCornerShape(corner / 2.4f),
    small = RoundedCornerShape(corner / 1.8f),
    medium = RoundedCornerShape(corner / 1.3f),
    large = RoundedCornerShape(corner),
    extraLarge = RoundedCornerShape(corner + 4.dp)
)

// ---------------------------------------------------------------- تم: شفق قطبی (تنها تم برنامه)
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

/** همه تم‌های موجود، به ترتیب نمایش در تنظیمات. فعلاً فقط «شفق قطبی». */
val AllSkins: List<AppSkin> = listOf(AuroraSkin)

fun skinOf(palette: Palette): AppSkin = when (palette) {
    Palette.AURORA -> AuroraSkin
}

val LocalAppSkin: ProvidableCompositionLocal<AppSkin> = compositionLocalOf { AuroraSkin }

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
        Palette.AURORA -> AuroraScheme
    }
    CompositionLocalProvider(LocalAppSkin provides skin) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography(),
            shapes = shapesFor(skin.cardCorner),
            content = content
        )
    }
}
