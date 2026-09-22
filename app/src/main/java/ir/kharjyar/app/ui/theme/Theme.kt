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
    val backdropImageAlpha: Float = 1f
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

// ================================================================ تم ۱: شفق قطبی
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
    subtitle = "سرمه‌ای عمیق با موج‌های زمردی و بنفش",
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
    onHero = Color(0xFFE2F4F1),
    fabGradient = listOf(Color(0xFF2BE4A8), Color(0xFF7C5CFF)),
    accent = Color(0xFF2BE4A8),
    incomeColor = Color(0xFF2BE4A8),
    expenseColor = Color(0xFFFF8E9E),
    chartGlow = true,
    bigNumberColor = Color(0xFF9CFFDF),
    onBackdrop = Color(0xFFE2F4F1),
    dialogColor = Color(0xFF103241),
    navBarColor = Color(0xFF0C2C39),
    navSelected = Color(0xFF2BE4A8),
    navUnselected = Color(0xFF7FA3AD)
)

// ================================================================ تم ۲: زمرد شب
private val EmeraldScheme = darkColorScheme(
    primary = Color(0xFF35D98A), onPrimary = Color(0xFF00291A),
    primaryContainer = Color(0xFF0B4A31), onPrimaryContainer = Color(0xFFB4F5D5),
    secondary = Color(0xFFE0A33C), onSecondary = Color(0xFF2E1D00),
    secondaryContainer = Color(0xFF4B3510), onSecondaryContainer = Color(0xFFFFE2B0),
    tertiary = Color(0xFF7FD9C0), onTertiary = Color(0xFF00322A),
    tertiaryContainer = Color(0xFF0C4A3F), onTertiaryContainer = Color(0xFFC0F5E8),
    background = Color(0xFF0A1410), onBackground = Color(0xFFDDEFE5),
    surface = Color(0xFF10201A), onSurface = Color(0xFFDDEFE5),
    surfaceVariant = Color(0xFF1A2E25), onSurfaceVariant = Color(0xFFAAC4B7),
    outline = Color(0x6635D98A), outlineVariant = Color(0x2635D98A),
    error = Color(0xFFFF9B84), onError = Color(0xFF3B1000),
    errorContainer = Color(0xFF5A1E08), onErrorContainer = Color(0xFFFFDBD1)
)

private val EmeraldSkin = AppSkin(
    id = Palette.EMERALD,
    title = "زمرد شب",
    subtitle = "مشکی جنگلی مات با اعداد زمردی",
    dark = true,
    backgroundColors = listOf(Color(0xFF0A1410), Color(0xFF08110D)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x2635D98A), 0.8f, 0.12f, 0.08f),
        AppSkin.Blob(Color(0x1AE0A33C), 0.7f, 0.95f, 0.55f)
    ),
    cardColor = Color(0xFF10201A),
    cardAlpha = 1f,
    cardBorderColors = listOf(Color(0x5935D98A), Color(0x2635D98A)),
    cardBorderWidth = 1.dp,
    cardCorner = 24.dp,
    heroGradient = listOf(Color(0xFF0E3226), Color(0xFF0A1F18)),
    onHero = Color(0xFFDDEFE5),
    fabGradient = listOf(Color(0xFF35D98A), Color(0xFF1FA968)),
    accent = Color(0xFF35D98A),
    incomeColor = Color(0xFF35D98A),
    expenseColor = Color(0xFFE0A33C),
    chartGlow = true,
    bigNumberColor = Color(0xFF6BEFAE),
    onBackdrop = Color(0xFFDDEFE5),
    dialogColor = Color(0xFF10201A),
    navBarColor = Color(0xFF0A1A14),
    navSelected = Color(0xFF35D98A),
    navUnselected = Color(0xFF6E8A7D)
)

// ================================================================ تم ۳: کاغذ ساده
private val PaperScheme = lightColorScheme(
    primary = Color(0xFF2F6BFF), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE7FF), onPrimaryContainer = Color(0xFF00194A),
    secondary = Color(0xFF4A4F57), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E8EC), onSecondaryContainer = Color(0xFF15181D),
    tertiary = Color(0xFF1F7A54), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCDEEDF), onTertiaryContainer = Color(0xFF002417),
    background = Color(0xFFF7F7F5), onBackground = Color(0xFF15171A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF15171A),
    surfaceVariant = Color(0xFFEFEFEC), onSurfaceVariant = Color(0xFF5A5E66),
    outline = Color(0xFFD5D6D2), outlineVariant = Color(0xFFE6E7E3),
    error = Color(0xFFC23934), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002)
)

private val PaperSkin = AppSkin(
    id = Palette.PAPER,
    title = "کاغذ ساده",
    subtitle = "روشن، بدون گرادیان، با تمرکز بر خوانایی",
    dark = false,
    backgroundColors = listOf(Color(0xFFF7F7F5), Color(0xFFF7F7F5)),
    backdropBlobs = emptyList(),
    cardColor = Color(0xFFFFFFFF),
    cardAlpha = 1f,
    cardBorderColors = listOf(Color(0xFFDFDFDB), Color(0xFFDFDFDB)),
    cardBorderWidth = 1.dp,
    cardCorner = 16.dp,
    heroGradient = listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF)),
    onHero = Color(0xFF15171A),
    fabGradient = listOf(Color(0xFF2F6BFF), Color(0xFF2F6BFF)),
    accent = Color(0xFF2F6BFF),
    incomeColor = Color(0xFF2F6BFF),
    expenseColor = Color(0xFF8A8F98),
    chartGlow = false,
    bigNumberColor = Color(0xFF0B0C0E),
    onBackdrop = Color(0xFF15171A),
    dialogColor = Color(0xFFFFFFFF),
    navBarColor = Color(0xFFFFFFFF),
    navSelected = Color(0xFF2F6BFF),
    navUnselected = Color(0xFF9A9EA6)
)

// ================================================================ تم ۴: ارغوان شاهانه
private val PlumScheme = darkColorScheme(
    primary = Color(0xFFD4AF37), onPrimary = Color(0xFF2A1D00),
    primaryContainer = Color(0xFF4A3708), onPrimaryContainer = Color(0xFFFFE9A8),
    secondary = Color(0xFFE57BA6), onSecondary = Color(0xFF3E0721),
    secondaryContainer = Color(0xFF5C1739), onSecondaryContainer = Color(0xFFFFD9E6),
    tertiary = Color(0xFFC9A4F0), onTertiary = Color(0xFF29104A),
    tertiaryContainer = Color(0xFF3F2263), onTertiaryContainer = Color(0xFFEEDCFF),
    background = Color(0xFF1E0B2E), onBackground = Color(0xFFF3E7DC),
    surface = Color(0xFF2A1240), onSurface = Color(0xFFF3E7DC),
    surfaceVariant = Color(0xFF3A1D52), onSurfaceVariant = Color(0xFFCDB8DC),
    outline = Color(0x99D4AF37), outlineVariant = Color(0x40D4AF37),
    error = Color(0xFFE57BA6), onError = Color(0xFF3E0721),
    errorContainer = Color(0xFF5C1739), onErrorContainer = Color(0xFFFFD9E6)
)

private val PlumSkin = AppSkin(
    id = Palette.PLUM,
    title = "ارغوان شاهانه",
    subtitle = "بنفش عمیق با حاشیه‌های طلایی",
    dark = true,
    backgroundColors = listOf(Color(0xFF1E0B2E), Color(0xFF160821)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x33D4AF37), 0.6f, 0.9f, 0.08f),
        AppSkin.Blob(Color(0x407B2E8E), 0.9f, 0.1f, 0.4f),
        AppSkin.Blob(Color(0x26E57BA6), 0.7f, 0.6f, 0.95f)
    ),
    cardColor = Color(0xFF2A1240),
    cardAlpha = 0.9f,
    cardBorderColors = listOf(Color(0xFFD4AF37), Color(0x66D4AF37)),
    cardBorderWidth = 1.2.dp,
    cardCorner = 24.dp,
    heroGradient = listOf(Color(0xFF4A1A6B), Color(0xFF8E2A6B)),
    onHero = Color(0xFFFFF3DC),
    fabGradient = listOf(Color(0xFFF0CC5E), Color(0xFFC49A28)),
    accent = Color(0xFFD4AF37),
    incomeColor = Color(0xFFD4AF37),
    expenseColor = Color(0xFFE57BA6),
    chartGlow = false,
    bigNumberColor = Color(0xFFFFF3DC),
    onBackdrop = Color(0xFFF3E7DC),
    dialogColor = Color(0xFF2A1240),
    navBarColor = Color(0xFF250F38),
    navSelected = Color(0xFFD4AF37),
    navUnselected = Color(0xFF9B83AD)
)

// ================================================================ تم ۵: سنگی نعنایی
private val SlateScheme = darkColorScheme(
    primary = Color(0xFF4FD1A5), onPrimary = Color(0xFF00281B),
    primaryContainer = Color(0xFF0C4735), onPrimaryContainer = Color(0xFFC3F5E2),
    secondary = Color(0xFFFF7A6B), onSecondary = Color(0xFF3E0A03),
    secondaryContainer = Color(0xFF5E1A10), onSecondaryContainer = Color(0xFFFFDAD3),
    tertiary = Color(0xFF9FB3C0), onTertiary = Color(0xFF16232B),
    tertiaryContainer = Color(0xFF2C3A44), onTertiaryContainer = Color(0xFFD8E6EE),
    background = Color(0xFF222A31), onBackground = Color(0xFFEDF1F4),
    surface = Color(0xFF2E383F), onSurface = Color(0xFFEDF1F4),
    surfaceVariant = Color(0xFF39444C), onSurfaceVariant = Color(0xFFBCC7CE),
    outline = Color(0x4DFFFFFF), outlineVariant = Color(0x26FFFFFF),
    error = Color(0xFFFF7A6B), onError = Color(0xFF3E0A03),
    errorContainer = Color(0xFF5E1A10), onErrorContainer = Color(0xFFFFDAD3)
)

private val SlateSkin = AppSkin(
    id = Palette.SLATE,
    title = "سنگی نعنایی",
    subtitle = "خاکستری ملایم با کارت نعنایی پررنگ",
    dark = true,
    backgroundColors = listOf(Color(0xFF222A31), Color(0xFF1C2329)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x1A4FD1A5), 0.8f, 0.15f, 0.1f),
        AppSkin.Blob(Color(0x14FF7A6B), 0.7f, 0.9f, 0.5f)
    ),
    cardColor = Color(0xFF2E383F),
    cardAlpha = 1f,
    cardBorderColors = listOf(Color(0x1AFFFFFF), Color(0x1AFFFFFF)),
    cardBorderWidth = 1.dp,
    cardCorner = 28.dp,
    heroGradient = listOf(Color(0xFF6FE0BC), Color(0xFF3FBF93)),
    // کارت hero روشن است؛ متن روی آن باید تیره باشد
    onHero = Color(0xFF12241D),
    fabGradient = listOf(Color(0xFF6FE0BC), Color(0xFF3FBF93)),
    accent = Color(0xFF4FD1A5),
    incomeColor = Color(0xFF4FD1A5),
    expenseColor = Color(0xFFFF7A6B),
    chartGlow = false,
    bigNumberColor = Color(0xFFEDF1F4),
    onBackdrop = Color(0xFFEDF1F4),
    dialogColor = Color(0xFF2E383F),
    navBarColor = Color(0xFF1B2228),
    navSelected = Color(0xFF4FD1A5),
    navUnselected = Color(0xFF8A99A3)
)


// ================================================================ تم ۶: طلای شاهانه
private val GoldScheme = darkColorScheme(
    primary = Color(0xFFE8C570), onPrimary = Color(0xFF2A1F00),
    primaryContainer = Color(0xFF4A3A0C), onPrimaryContainer = Color(0xFFFFEDBD),
    secondary = Color(0xFFD9A441), onSecondary = Color(0xFF2B1C00),
    secondaryContainer = Color(0xFF43320A), onSecondaryContainer = Color(0xFFFFE6B5),
    tertiary = Color(0xFF5BC9A0), onTertiary = Color(0xFF00281A),
    tertiaryContainer = Color(0xFF0D4632), onTertiaryContainer = Color(0xFFBFF2DD),
    background = Color(0xFF0B0B0D), onBackground = Color(0xFFF2EADA),
    surface = Color(0xFF141416), onSurface = Color(0xFFF2EADA),
    surfaceVariant = Color(0xFF1E1E21), onSurfaceVariant = Color(0xFFC3B89F),
    outline = Color(0x66E8C570), outlineVariant = Color(0x33E8C570),
    error = Color(0xFFF07A6E), onError = Color(0xFF3B0A05),
    errorContainer = Color(0xFF5C170F), onErrorContainer = Color(0xFFFFDAD4)
)

private val GoldSkin = AppSkin(
    id = Palette.GOLD,
    title = "طلای شاهانه",
    subtitle = "مشکی مات با کارت طلایی و نقش اسلیمی",
    dark = true,
    backgroundColors = listOf(Color(0xFF0B0B0D), Color(0xFF09090B)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x1FE8C570), 0.7f, 0.12f, 0.06f),
        AppSkin.Blob(Color(0x14D9A441), 0.8f, 0.92f, 0.6f)
    ),
    cardColor = Color(0xFF141416),
    cardAlpha = 1f,
    cardBorderColors = listOf(Color(0x59E8C570), Color(0x26E8C570)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFFF3D284), Color(0xFFDDA83E)),
    onHero = Color(0xFF3A2A05),
    fabGradient = listOf(Color(0xFFF3D284), Color(0xFFD9A441)),
    accent = Color(0xFFE8C570),
    incomeColor = Color(0xFF5BC9A0),
    expenseColor = Color(0xFFF07A6E),
    chartGlow = false,
    bigNumberColor = Color(0xFFF7E7BF),
    onBackdrop = Color(0xFFF2EADA),
    dialogColor = Color(0xFF16161A),
    navBarColor = Color(0xFF0E0E10),
    navSelected = Color(0xFFE8C570),
    navUnselected = Color(0xFF7A7568),
    heroImage = R.drawable.hero_gold
)

// ================================================================ تم ۷: شکوفه شب
private val SakuraScheme = darkColorScheme(
    primary = Color(0xFFE86FA8), onPrimary = Color(0xFF3E0424),
    primaryContainer = Color(0xFF5E1440), onPrimaryContainer = Color(0xFFFFD8E8),
    secondary = Color(0xFF9B6BF0), onSecondary = Color(0xFF210A4A),
    secondaryContainer = Color(0xFF3A1D6E), onSecondaryContainer = Color(0xFFE6D8FF),
    tertiary = Color(0xFF6FD3B4), onTertiary = Color(0xFF00291F),
    tertiaryContainer = Color(0xFF0C4636), onTertiaryContainer = Color(0xFFBDF3E2),
    background = Color(0xFF150C1B), onBackground = Color(0xFFF3E6F2),
    surface = Color(0xFF1E1226), onSurface = Color(0xFFF3E6F2),
    surfaceVariant = Color(0xFF2A1A33), onSurfaceVariant = Color(0xFFC9B4CE),
    outline = Color(0x4DE86FA8), outlineVariant = Color(0x269B6BF0),
    error = Color(0xFFF2627E), onError = Color(0xFF3F0314),
    errorContainer = Color(0xFF5E1027), onErrorContainer = Color(0xFFFFD9E0)
)

private val SakuraSkin = AppSkin(
    id = Palette.SAKURA,
    title = "شکوفه شب",
    subtitle = "بنفش تاریک با کارت صورتی‌بنفش و شکوفه",
    dark = true,
    backgroundColors = listOf(Color(0xFF150C1B), Color(0xFF100814)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x2EE86FA8), 0.8f, 0.1f, 0.08f),
        AppSkin.Blob(Color(0x269B6BF0), 0.9f, 0.95f, 0.55f)
    ),
    cardColor = Color(0xFF1E1226),
    cardAlpha = 0.92f,
    cardBorderColors = listOf(Color(0x40E86FA8), Color(0x339B6BF0)),
    cardBorderWidth = 1.dp,
    cardCorner = 24.dp,
    heroGradient = listOf(Color(0xFFF07AAE), Color(0xFF9B6BF0)),
    onHero = Color(0xFFFFFFFF),
    fabGradient = listOf(Color(0xFFF07AAE), Color(0xFF9B6BF0)),
    accent = Color(0xFFE86FA8),
    incomeColor = Color(0xFF6FD3B4),
    expenseColor = Color(0xFFF2627E),
    chartGlow = true,
    bigNumberColor = Color(0xFFFFE3F0),
    onBackdrop = Color(0xFFF3E6F2),
    dialogColor = Color(0xFF1E1226),
    navBarColor = Color(0xFF150D1C),
    navSelected = Color(0xFFE86FA8),
    navUnselected = Color(0xFF8B7A94),
    heroImage = R.drawable.hero_sakura,
    backdropImage = R.drawable.bg_sakura,
    backdropImageAlpha = 0.75f
)

// ================================================================ تم ۸: اعماق اقیانوس
private val OceanScheme = darkColorScheme(
    primary = Color(0xFF29B6F6), onPrimary = Color(0xFF002233),
    primaryContainer = Color(0xFF00405C), onPrimaryContainer = Color(0xFFBFE9FF),
    secondary = Color(0xFF26D1D1), onSecondary = Color(0xFF002B2B),
    secondaryContainer = Color(0xFF004545), onSecondaryContainer = Color(0xFFB5F5F5),
    tertiary = Color(0xFF7FB2E5), onTertiary = Color(0xFF032645),
    tertiaryContainer = Color(0xFF123F66), onTertiaryContainer = Color(0xFFD3E7FF),
    background = Color(0xFF071019), onBackground = Color(0xFFE3F1FA),
    surface = Color(0xFF0D1B27), onSurface = Color(0xFFE3F1FA),
    surfaceVariant = Color(0xFF16293A), onSurfaceVariant = Color(0xFFA8C2D4),
    outline = Color(0x4D29B6F6), outlineVariant = Color(0x2629B6F6),
    error = Color(0xFFE8614E), onError = Color(0xFF3B0A02),
    errorContainer = Color(0xFF5C170B), onErrorContainer = Color(0xFFFFDAD2)
)

private val OceanSkin = AppSkin(
    id = Palette.OCEAN,
    title = "اعماق اقیانوس",
    subtitle = "سرمه‌ای شب با کارت فیروزه‌ای و پرتو نور",
    dark = true,
    backgroundColors = listOf(Color(0xFF071019), Color(0xFF040A11)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x2629B6F6), 0.9f, 0.5f, 0.02f),
        AppSkin.Blob(Color(0x1A26D1D1), 0.8f, 0.9f, 0.5f)
    ),
    cardColor = Color(0xFF0D1B27),
    cardAlpha = 0.9f,
    cardBorderColors = listOf(Color(0x4029B6F6), Color(0x2626D1D1)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFF1477D6), Color(0xFF22C7E8)),
    onHero = Color(0xFFEAFAFF),
    fabGradient = listOf(Color(0xFF29B6F6), Color(0xFF1477D6)),
    accent = Color(0xFF29B6F6),
    incomeColor = Color(0xFF26D1D1),
    expenseColor = Color(0xFFE8614E),
    chartGlow = true,
    bigNumberColor = Color(0xFFBFF0FF),
    onBackdrop = Color(0xFFE3F1FA),
    dialogColor = Color(0xFF0D1B27),
    navBarColor = Color(0xFF060E16),
    navSelected = Color(0xFF29B6F6),
    navUnselected = Color(0xFF6C8698),
    heroImage = R.drawable.hero_ocean,
    backdropImage = R.drawable.bg_ocean,
    backdropImageAlpha = 0.8f
)

/** همه تم‌های موجود، به ترتیب نمایش در تنظیمات. */
val AllSkins: List<AppSkin> = listOf(
    GoldSkin, SakuraSkin, OceanSkin, AuroraSkin, EmeraldSkin, PlumSkin, SlateSkin, PaperSkin
)

fun skinOf(palette: Palette): AppSkin = when (palette) {
    Palette.AURORA -> AuroraSkin
    Palette.EMERALD -> EmeraldSkin
    Palette.PAPER -> PaperSkin
    Palette.PLUM -> PlumSkin
    Palette.SLATE -> SlateSkin
    Palette.GOLD -> GoldSkin
    Palette.SAKURA -> SakuraSkin
    Palette.OCEAN -> OceanSkin
}

val LocalAppSkin: ProvidableCompositionLocal<AppSkin> = compositionLocalOf { AuroraSkin }

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
        Palette.AURORA -> AuroraScheme
        Palette.EMERALD -> EmeraldScheme
        Palette.PAPER -> PaperScheme
        Palette.PLUM -> PlumScheme
        Palette.SLATE -> SlateScheme
        Palette.GOLD -> GoldScheme
        Palette.SAKURA -> SakuraScheme
        Palette.OCEAN -> OceanScheme
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
