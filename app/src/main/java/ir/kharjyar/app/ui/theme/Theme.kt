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
 * با CompositionLocal در کل برنامه (نمودار، دیالوگ، کارت‌ها، نوار پایین، ویجت) در دسترس است.
 */
data class AppSkin(
    val id: Palette,
    val title: String,
    /** توضیح کوتاه برای کارت انتخاب تم در تنظیمات. */
    val subtitle: String,
    val dark: Boolean,
    val backgroundColors: List<Color>,
    val backdropBlobs: List<Blob>,
    val cardColor: Color,
    val cardAlpha: Float,
    val cardBorderColors: List<Color>,
    val cardBorderWidth: Dp,
    val cardCorner: Dp,
    val heroGradient: List<Color>,
    /** رنگ متن روی کارت hero (بعضی تم‌ها hero روشن دارند). */
    val onHero: Color,
    val fabGradient: List<Color>,
    val accent: Color,
    val incomeColor: Color,
    val expenseColor: Color,
    val chartGlow: Boolean,
    val bigNumberColor: Color,
    val onBackdrop: Color,
    val dialogColor: Color,
    /** رنگ پس‌زمینه نوار پایین. */
    val navBarColor: Color,
    val navSelected: Color,
    val navUnselected: Color,
    /** تصویر پس‌زمینه کارت hero (drawable) — اگر null باشد فقط گرادیان رسم می‌شود. */
    val heroImage: Int? = null,
    /** تصویر پس‌زمینه کل صفحه — اگر null باشد فقط گرادیان و blobها رسم می‌شوند. */
    val backdropImage: Int? = null,
    /** شفافیت تصویر پس‌زمینه صفحه. */
    val backdropImageAlpha: Float = 1f,
    /** رنگ‌های قاب نئونی دور کارت اصلی صفحه خانه. */
    val neonColors: List<Color> = emptyList()
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

// ================================================================ تم ۱: شکوفه نئون
private val SakuraScheme = darkColorScheme(
    primary = Color(0xFFFF3DAF), onPrimary = Color(0xFF3B0026),
    primaryContainer = Color(0xFF241030), onPrimaryContainer = Color(0xFFFCE9F6),
    secondary = Color(0xFFB44DFF), onSecondary = Color(0xFF3B0026),
    secondaryContainer = Color(0xFF241030), onSecondaryContainer = Color(0xFFFCE9F6),
    tertiary = Color(0xFF4DE8C2), onTertiary = Color(0xFF3B0026),
    tertiaryContainer = Color(0xFF241030), onTertiaryContainer = Color(0xFFFCE9F6),
    background = Color(0xFF0B0510), onBackground = Color(0xFFFCE9F6),
    surface = Color(0xFF160A1E), onSurface = Color(0xFFFCE9F6),
    surfaceVariant = Color(0xFF241030), onSurfaceVariant = Color(0xFFD8B8CE),
    outline = Color(0x80FF3DAF), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF5C7A), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val SakuraSkin = AppSkin(
    id = Palette.SAKURA,
    title = "شکوفه نئون",
    subtitle = "سرخابی پرانرژی با شکوفه و قاب نئونی",
    dark = true,
    backgroundColors = listOf(Color(0xFF0B0510), Color(0xFF07030B)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x24FF3DAF), 0.85f, 0.12f, 0.06f),
        AppSkin.Blob(Color(0x1CB44DFF), 0.9f, 0.92f, 0.5f)
    ),
    cardColor = Color(0xFF160A1E),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x59FF3DAF), Color(0x40B44DFF)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFFE81E9B), Color(0xFF8B2FD6)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFFFF3DAF), Color(0xFF8B2FD6)),
    accent = Color(0xFFFF3DAF),
    incomeColor = Color(0xFF4DE8C2),
    expenseColor = Color(0xFFFF5C7A),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFFCE9F6),
    dialogColor = Color(0xFF160A1E),
    navBarColor = Color(0xFF0C0512),
    navSelected = Color(0xFFFF3DAF),
    navUnselected = Color(0xFF8B7A94),
    heroImage = R.drawable.hero_sakura_neon,
    neonColors = listOf(Color(0xFFFF3DAF), Color(0xFFB44DFF))
)

// ================================================================ تم ۲: موج نیلی
private val IndigoScheme = darkColorScheme(
    primary = Color(0xFF2E7CFF), onPrimary = Color(0xFF001636),
    primaryContainer = Color(0xFF141C42), onPrimaryContainer = Color(0xFFE6ECFF),
    secondary = Color(0xFF9B5CFF), onSecondary = Color(0xFF001636),
    secondaryContainer = Color(0xFF141C42), onSecondaryContainer = Color(0xFFE6ECFF),
    tertiary = Color(0xFF3DD6C0), onTertiary = Color(0xFF001636),
    tertiaryContainer = Color(0xFF141C42), onTertiaryContainer = Color(0xFFE6ECFF),
    background = Color(0xFF05081A), onBackground = Color(0xFFE6ECFF),
    surface = Color(0xFF0C1230), onSurface = Color(0xFFE6ECFF),
    surfaceVariant = Color(0xFF141C42), onSurfaceVariant = Color(0xFFAEB9DB),
    outline = Color(0x802E7CFF), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF6B8A), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val IndigoSkin = AppSkin(
    id = Palette.INDIGO,
    title = "موج نیلی",
    subtitle = "سرمه‌ای شب با موج‌های آبی و بنفش",
    dark = true,
    backgroundColors = listOf(Color(0xFF05081A), Color(0xFF030510)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x242E7CFF), 0.85f, 0.9f, 0.08f),
        AppSkin.Blob(Color(0x1C9B5CFF), 0.9f, 0.1f, 0.55f)
    ),
    cardColor = Color(0xFF0C1230),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x592E7CFF), Color(0x409B5CFF)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF0B1038), Color(0xFF3A1C96)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFF2E7CFF), Color(0xFF9B5CFF)),
    accent = Color(0xFF2E7CFF),
    incomeColor = Color(0xFF3DD6C0),
    expenseColor = Color(0xFFFF6B8A),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFE6ECFF),
    dialogColor = Color(0xFF0C1230),
    navBarColor = Color(0xFF060A1C),
    navSelected = Color(0xFF2E7CFF),
    navUnselected = Color(0xFF7180A8),
    heroImage = R.drawable.hero_indigo_wave,
    neonColors = listOf(Color(0xFF2E7CFF), Color(0xFF9B5CFF))
)

// ================================================================ تم ۳: بنفش تیره
private val VioletScheme = darkColorScheme(
    primary = Color(0xFFA855F7), onPrimary = Color(0xFF250040),
    primaryContainer = Color(0xFF1F1233), onPrimaryContainer = Color(0xFFEDE4F7),
    secondary = Color(0xFFEC4899), onSecondary = Color(0xFF250040),
    secondaryContainer = Color(0xFF1F1233), onSecondaryContainer = Color(0xFFEDE4F7),
    tertiary = Color(0xFF34D399), onTertiary = Color(0xFF250040),
    tertiaryContainer = Color(0xFF1F1233), onTertiaryContainer = Color(0xFFEDE4F7),
    background = Color(0xFF0A0614), onBackground = Color(0xFFEDE4F7),
    surface = Color(0xFF140C22), onSurface = Color(0xFFEDE4F7),
    surfaceVariant = Color(0xFF1F1233), onSurfaceVariant = Color(0xFFBFAFD4),
    outline = Color(0x80A855F7), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFF43F6E), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val VioletSkin = AppSkin(
    id = Palette.VIOLET,
    title = "بنفش تیره",
    subtitle = "بنفش عمیق با خط‌های نئونی مورب",
    dark = true,
    backgroundColors = listOf(Color(0xFF0A0614), Color(0xFF06030D)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x20A855F7), 0.8f, 0.08f, 0.1f),
        AppSkin.Blob(Color(0x18EC4899), 0.85f, 0.95f, 0.6f)
    ),
    cardColor = Color(0xFF140C22),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x59A855F7), Color(0x40EC4899)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF1A0E2E), Color(0xFF2E1548)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFFA855F7), Color(0xFFEC4899)),
    accent = Color(0xFFA855F7),
    incomeColor = Color(0xFF34D399),
    expenseColor = Color(0xFFF43F6E),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFEDE4F7),
    dialogColor = Color(0xFF140C22),
    navBarColor = Color(0xFF080512),
    navSelected = Color(0xFFA855F7),
    navUnselected = Color(0xFF7D6E94),
    heroImage = R.drawable.hero_violet_stripe,
    neonColors = listOf(Color(0xFFA855F7), Color(0xFFEC4899))
)

// ================================================================ تم ۴: نیلوفر
private val LotusScheme = darkColorScheme(
    primary = Color(0xFFC77DFF), onPrimary = Color(0xFF2A0845),
    primaryContainer = Color(0xFF2E1A4D), onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFFFF8FD0), onSecondary = Color(0xFF2A0845),
    secondaryContainer = Color(0xFF2E1A4D), onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = Color(0xFF5EE6B8), onTertiary = Color(0xFF2A0845),
    tertiaryContainer = Color(0xFF2E1A4D), onTertiaryContainer = Color(0xFFF3E8FF),
    background = Color(0xFF150A26), onBackground = Color(0xFFF3E8FF),
    surface = Color(0xFF21123A), onSurface = Color(0xFFF3E8FF),
    surfaceVariant = Color(0xFF2E1A4D), onSurfaceVariant = Color(0xFFC9B3E0),
    outline = Color(0x80C77DFF), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF7095), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val LotusSkin = AppSkin(
    id = Palette.LOTUS,
    title = "نیلوفر",
    subtitle = "بنفش روشن با نیلوفر درخشان",
    dark = true,
    backgroundColors = listOf(Color(0xFF150A26), Color(0xFF0E0619)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x28C77DFF), 0.85f, 0.9f, 0.12f),
        AppSkin.Blob(Color(0x20FF8FD0), 0.8f, 0.1f, 0.6f)
    ),
    cardColor = Color(0xFF21123A),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x59C77DFF), Color(0x40FF8FD0)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF6B3FA8), Color(0xFF9B5FD0)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFFC77DFF), Color(0xFFFF8FD0)),
    accent = Color(0xFFC77DFF),
    incomeColor = Color(0xFF5EE6B8),
    expenseColor = Color(0xFFFF7095),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFF3E8FF),
    dialogColor = Color(0xFF21123A),
    navBarColor = Color(0xFF110820),
    navSelected = Color(0xFFC77DFF),
    navUnselected = Color(0xFF8B7AA8),
    heroImage = R.drawable.hero_lotus,
    neonColors = listOf(Color(0xFFC77DFF), Color(0xFFFF8FD0))
)

// ================================================================ تم ۵: شب مطلق
private val MidnightScheme = darkColorScheme(
    primary = Color(0xFFFF2D95), onPrimary = Color(0xFF3A0020),
    primaryContainer = Color(0xFF16161F), onPrimaryContainer = Color(0xFFF0F0F5),
    secondary = Color(0xFF22D3EE), onSecondary = Color(0xFF3A0020),
    secondaryContainer = Color(0xFF16161F), onSecondaryContainer = Color(0xFFF0F0F5),
    tertiary = Color(0xFF4ADE80), onTertiary = Color(0xFF3A0020),
    tertiaryContainer = Color(0xFF16161F), onTertiaryContainer = Color(0xFFF0F0F5),
    background = Color(0xFF000000), onBackground = Color(0xFFF0F0F5),
    surface = Color(0xFF0B0B10), onSurface = Color(0xFFF0F0F5),
    surfaceVariant = Color(0xFF16161F), onSurfaceVariant = Color(0xFFA8A8B8),
    outline = Color(0x80FF2D95), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF4D6D), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val MidnightSkin = AppSkin(
    id = Palette.MIDNIGHT,
    title = "شب مطلق",
    subtitle = "مشکی خالص با نئون سرخابی و فیروزه‌ای",
    dark = true,
    backgroundColors = listOf(Color(0xFF000000), Color(0xFF040406)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x1AFF2D95), 0.8f, 0.05f, 0.9f),
        AppSkin.Blob(Color(0x1422D3EE), 0.8f, 0.95f, 0.1f)
    ),
    cardColor = Color(0xFF0B0B10),
    cardAlpha = 0.96f,
    cardBorderColors = listOf(Color(0x59FF2D95), Color(0x4022D3EE)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF0A0A0F), Color(0xFF14101C)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFFFF2D95), Color(0xFF22D3EE)),
    accent = Color(0xFFFF2D95),
    incomeColor = Color(0xFF4ADE80),
    expenseColor = Color(0xFFFF4D6D),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFF0F0F5),
    dialogColor = Color(0xFF0B0B10),
    navBarColor = Color(0xFF000000),
    navSelected = Color(0xFFFF2D95),
    navUnselected = Color(0xFF6E6E80),
    heroImage = R.drawable.hero_dark_ring,
    neonColors = listOf(Color(0xFFFF2D95), Color(0xFF22D3EE))
)

// ================================================================ تم ۶: غروب
private val SunsetScheme = darkColorScheme(
    primary = Color(0xFFFF7A59), onPrimary = Color(0xFF3B1000),
    primaryContainer = Color(0xFF2A1A42), onPrimaryContainer = Color(0xFFFCEAE4),
    secondary = Color(0xFFC77DFF), onSecondary = Color(0xFF3B1000),
    secondaryContainer = Color(0xFF2A1A42), onSecondaryContainer = Color(0xFFFCEAE4),
    tertiary = Color(0xFF5EE6B8), onTertiary = Color(0xFF3B1000),
    tertiaryContainer = Color(0xFF2A1A42), onTertiaryContainer = Color(0xFFFCEAE4),
    background = Color(0xFF120A1F), onBackground = Color(0xFFFCEAE4),
    surface = Color(0xFF1D1230), onSurface = Color(0xFFFCEAE4),
    surfaceVariant = Color(0xFF2A1A42), onSurfaceVariant = Color(0xFFCBB3C8),
    outline = Color(0x80FF7A59), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF6B8A), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val SunsetSkin = AppSkin(
    id = Palette.SUNSET,
    title = "غروب",
    subtitle = "افق نارنجی و بنفش با کوه و دریاچه",
    dark = true,
    backgroundColors = listOf(Color(0xFF120A1F), Color(0xFF0B0615)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x24FF7A59), 0.8f, 0.5f, 0.05f),
        AppSkin.Blob(Color(0x1CC77DFF), 0.85f, 0.1f, 0.6f)
    ),
    cardColor = Color(0xFF1D1230),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x59FF7A59), Color(0x40C77DFF)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF7A3F8F), Color(0xFFD96A6A)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFFFF7A59), Color(0xFFC77DFF)),
    accent = Color(0xFFFF7A59),
    incomeColor = Color(0xFF5EE6B8),
    expenseColor = Color(0xFFFF6B8A),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFFCEAE4),
    dialogColor = Color(0xFF1D1230),
    navBarColor = Color(0xFF0E0819),
    navSelected = Color(0xFFFF7A59),
    navUnselected = Color(0xFF8B7A94),
    heroImage = R.drawable.hero_sunset,
    neonColors = listOf(Color(0xFFFF7A59), Color(0xFFC77DFF))
)

// ================================================================ تم ۷: اقیانوس
private val OceanScheme = darkColorScheme(
    primary = Color(0xFF22D3EE), onPrimary = Color(0xFF00303B),
    primaryContainer = Color(0xFF12293D), onPrimaryContainer = Color(0xFFE0F5FF),
    secondary = Color(0xFF3B82F6), onSecondary = Color(0xFF00303B),
    secondaryContainer = Color(0xFF12293D), onSecondaryContainer = Color(0xFFE0F5FF),
    tertiary = Color(0xFF4ADE80), onTertiary = Color(0xFF00303B),
    tertiaryContainer = Color(0xFF12293D), onTertiaryContainer = Color(0xFFE0F5FF),
    background = Color(0xFF04101A), onBackground = Color(0xFFE0F5FF),
    surface = Color(0xFF0A1B2A), onSurface = Color(0xFFE0F5FF),
    surfaceVariant = Color(0xFF12293D), onSurfaceVariant = Color(0xFFA3C4D6),
    outline = Color(0x8022D3EE), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFFF7A6B), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val OceanSkin = AppSkin(
    id = Palette.OCEAN,
    title = "اقیانوس",
    subtitle = "آبی عمیق با پرتوهای نور زیر آب",
    dark = true,
    backgroundColors = listOf(Color(0xFF04101A), Color(0xFF020A11)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x2822D3EE), 0.9f, 0.5f, 0.02f),
        AppSkin.Blob(Color(0x1C3B82F6), 0.85f, 0.9f, 0.55f)
    ),
    cardColor = Color(0xFF0A1B2A),
    cardAlpha = 0.95f,
    cardBorderColors = listOf(Color(0x5922D3EE), Color(0x403B82F6)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF0E5C86), Color(0xFF17A2C4)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFF22D3EE), Color(0xFF3B82F6)),
    accent = Color(0xFF22D3EE),
    incomeColor = Color(0xFF4ADE80),
    expenseColor = Color(0xFFFF7A6B),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFFFFF),
    onBackdrop = Color(0xFFE0F5FF),
    dialogColor = Color(0xFF0A1B2A),
    navBarColor = Color(0xFF040D16),
    navSelected = Color(0xFF22D3EE),
    navUnselected = Color(0xFF6B8598),
    heroImage = R.drawable.hero_ocean_neon,
    neonColors = listOf(Color(0xFF22D3EE), Color(0xFF3B82F6))
)

// ================================================================ تم ۸: طلای شاهانه
private val GoldScheme = darkColorScheme(
    primary = Color(0xFFF0C368), onPrimary = Color(0xFF2E2100),
    primaryContainer = Color(0xFF201C13), onPrimaryContainer = Color(0xFFF7EEDC),
    secondary = Color(0xFFD9A441), onSecondary = Color(0xFF2E2100),
    secondaryContainer = Color(0xFF201C13), onSecondaryContainer = Color(0xFFF7EEDC),
    tertiary = Color(0xFF5BC9A0), onTertiary = Color(0xFF2E2100),
    tertiaryContainer = Color(0xFF201C13), onTertiaryContainer = Color(0xFFF7EEDC),
    background = Color(0xFF0A0906), onBackground = Color(0xFFF7EEDC),
    surface = Color(0xFF14120C), onSurface = Color(0xFFF7EEDC),
    surfaceVariant = Color(0xFF201C13), onSurfaceVariant = Color(0xFFC4B69A),
    outline = Color(0x80F0C368), outlineVariant = Color(0x33FFFFFF),
    error = Color(0xFFF07A6E), onError = Color(0xFF3B0A10),
    errorContainer = Color(0xFF5C1020), onErrorContainer = Color(0xFFFFD9E0)
)

private val GoldSkin = AppSkin(
    id = Palette.GOLD,
    title = "طلای شاهانه",
    subtitle = "مشکی مات با نقش اسلیمی و قاب طلایی",
    dark = true,
    backgroundColors = listOf(Color(0xFF0A0906), Color(0xFF060504)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x1FF0C368), 0.8f, 0.9f, 0.08f),
        AppSkin.Blob(Color(0x14D9A441), 0.85f, 0.1f, 0.6f)
    ),
    cardColor = Color(0xFF14120C),
    cardAlpha = 0.96f,
    cardBorderColors = listOf(Color(0x59F0C368), Color(0x40D9A441)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF16130B), Color(0xFF2E2614)),
    onHero = Color(0xFFF7EEDC),
    fabGradient = listOf(Color(0xFFF0C368), Color(0xFFD9A441)),
    accent = Color(0xFFF0C368),
    incomeColor = Color(0xFF5BC9A0),
    expenseColor = Color(0xFFF07A6E),
    chartGlow = false,
    bigNumberColor = Color(0xFFFFF3DC),
    onBackdrop = Color(0xFFF7EEDC),
    dialogColor = Color(0xFF14120C),
    navBarColor = Color(0xFF080705),
    navSelected = Color(0xFFF0C368),
    navUnselected = Color(0xFF7A7263),
    heroImage = R.drawable.hero_gold_neon,
    neonColors = listOf(Color(0xFFF0C368), Color(0xFFD9A441))
)

/** همه تم‌های موجود، به ترتیب نمایش در تنظیمات. */
val AllSkins: List<AppSkin> = listOf(
    SakuraSkin, IndigoSkin, VioletSkin, LotusSkin, MidnightSkin, SunsetSkin, OceanSkin, GoldSkin
)

fun skinOf(palette: Palette): AppSkin = when (palette) {
    Palette.SAKURA -> SakuraSkin
    Palette.INDIGO -> IndigoSkin
    Palette.VIOLET -> VioletSkin
    Palette.LOTUS -> LotusSkin
    Palette.MIDNIGHT -> MidnightSkin
    Palette.SUNSET -> SunsetSkin
    Palette.OCEAN -> OceanSkin
    Palette.GOLD -> GoldSkin
}

val LocalAppSkin: ProvidableCompositionLocal<AppSkin> = compositionLocalOf { SakuraSkin }

@Suppress("UNUSED_PARAMETER")
@Composable
fun KharjYarTheme(
    themeMode: ThemeMode,
    palette: Palette,
    content: @Composable () -> Unit
) {
    // هر تم خودش روشن/تاریک بودن را تعریف می‌کند؛ ThemeMode برای سازگاری نگه داشته شده است.
    val skin = skinOf(palette)
    val colorScheme = when (palette) {
        Palette.SAKURA -> SakuraScheme
        Palette.INDIGO -> IndigoScheme
        Palette.VIOLET -> VioletScheme
        Palette.LOTUS -> LotusScheme
        Palette.MIDNIGHT -> MidnightScheme
        Palette.SUNSET -> SunsetScheme
        Palette.OCEAN -> OceanScheme
        Palette.GOLD -> GoldScheme
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
