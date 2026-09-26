package ir.kharjyar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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


// ================================================================ تم ۳: بنفش تیره
private val VioletScheme = lightColorScheme(
    primary = Color(0xFF7C3AED), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE4FF), onPrimaryContainer = Color(0xFF2A0D5E),
    secondary = Color(0xFFDB2777), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE0EF), onSecondaryContainer = Color(0xFF4A0424),
    tertiary = Color(0xFF0891B2), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFF3FB), onTertiaryContainer = Color(0xFF00323D),
    background = Color(0xFFF7F4FD), onBackground = Color(0xFF1C1626),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1C1626),
    surfaceVariant = Color(0xFFF1ECFA), onSurfaceVariant = Color(0xFF5B5470),
    outline = Color(0xFFD5CCE8), outlineVariant = Color(0xFFE8E1F5),
    error = Color(0xFFD1344F), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAE0), onErrorContainer = Color(0xFF400010)
)

private val VioletSkin = AppSkin(
    id = Palette.VIOLET,
    title = "روشنای روز",
    subtitle = "سفید و یاسی روشن، مناسب محیط پرنور",
    dark = false,
    backgroundColors = listOf(Color(0xFFF7F4FD), Color(0xFFFFFFFF)),
    backdropBlobs = listOf(
        AppSkin.Blob(Color(0x1A7C3AED), 0.8f, 0.08f, 0.08f),
        AppSkin.Blob(Color(0x14DB2777), 0.85f, 0.95f, 0.6f)
    ),
    cardColor = Color(0xFFFFFFFF),
    cardAlpha = 1f,
    cardBorderColors = listOf(Color(0x337C3AED), Color(0x26DB2777)),
    cardBorderWidth = 1.dp,
    cardCorner = 22.dp,
    heroGradient = listOf(Color(0xFFEDE4FF), Color(0xFFFCE7F3)),
    // کارت روشن است، پس متن روی آن باید تیره باشد
    onHero = Color(0xFF241A38),
    fabGradient = listOf(Color(0xFF7C3AED), Color(0xFFDB2777)),
    accent = Color(0xFF7C3AED),
    // سبز تیره‌تر تا روی سفید کنتراست کافی داشته باشد
    incomeColor = Color(0xFF047857),
    expenseColor = Color(0xFFC01C38),
    chartGlow = false,
    bigNumberColor = Color(0xFF241A38),
    onBackdrop = Color(0xFF1C1626),
    dialogColor = Color(0xFFFFFFFF),
    navBarColor = Color(0xFFFFFFFF),
    navSelected = Color(0xFF7C3AED),
    // خاکستری تیره‌تر برای آیکون‌های غیرفعال نوار پایین
    navUnselected = Color(0xFF6B6480),
    heroImage = R.drawable.hero_daylight,
    neonColors = listOf(Color(0xFF7C3AED), Color(0xFFDB2777))
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


// ================================================================ تم ۶: غروب


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
    heroGradient = listOf(Color(0xFF0E5C86), Color(0xFF1289AA)),
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

// ================================================================ مینیمال روز / شب
private val MinimalDayScheme = lightColorScheme(
    primary = Color(0xFF2563EB), onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE), onPrimaryContainer = Color(0xFF172554),
    secondary = Color(0xFF475569), onSecondary = Color.White,
    background = Color(0xFFF8FAFC), onBackground = Color(0xFF0F172A),
    surface = Color.White, onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1), error = Color(0xFFDC2626), onError = Color.White
)
private val MinimalNightScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF172554), onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF94A3B8), onSecondary = Color(0xFF0F172A),
    background = Color(0xFF090E17), onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827), onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E293B), onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155), error = Color(0xFFF87171), onError = Color(0xFF450A0A)
)

private val MinimalDaySkin = AppSkin(
    id = Palette.MINIMAL_DAY, title = "مینیمال روز", subtitle = "سفید، خلوت و خوانا برای روشنایی روز",
    dark = false, backgroundColors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)), backdropBlobs = emptyList(),
    cardColor = Color.White, cardAlpha = 1f, cardBorderColors = listOf(Color(0xFFCBD5E1)),
    cardBorderWidth = 1.dp, cardCorner = 18.dp,
    // کارت اصلی روز روشن و تمیز است؛ آبی فقط ته‌رنگ دارد تا اعداد تخت و تیره واضح بمانند.
    heroGradient = listOf(Color(0xFFFFFFFF), Color(0xFFF5F9FF), Color(0xFFE8F1FF)),
    onHero = Color(0xFF10233F), fabGradient = listOf(Color(0xFF2563EB), Color(0xFF3B82F6)),
    accent = Color(0xFF2563EB), incomeColor = Color(0xFF15803D), expenseColor = Color(0xFFDC2626),
    chartGlow = false, bigNumberColor = Color(0xFF0F172A), onBackdrop = Color(0xFF0F172A),
    dialogColor = Color.White, navBarColor = Color.White, navSelected = Color(0xFF2563EB),
    navUnselected = Color(0xFF64748B)
)
private val MinimalNightSkin = AppSkin(
    id = Palette.MINIMAL_NIGHT, title = "مینیمال شب", subtitle = "تیره، آرام و بدون تزئین اضافه",
    dark = true, backgroundColors = listOf(Color(0xFF090E17), Color(0xFF0F172A)), backdropBlobs = emptyList(),
    cardColor = Color(0xFF111827), cardAlpha = 1f, cardBorderColors = listOf(Color(0xFF334155)),
    cardBorderWidth = 1.dp, cardCorner = 16.dp, heroGradient = listOf(Color(0xFF172554), Color(0xFF1E3A5F)),
    onHero = Color(0xFFE2E8F0), fabGradient = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
    accent = Color(0xFF60A5FA), incomeColor = Color(0xFF4ADE80), expenseColor = Color(0xFFF87171),
    chartGlow = false, bigNumberColor = Color(0xFFE2E8F0), onBackdrop = Color(0xFFE2E8F0),
    dialogColor = Color(0xFF111827), navBarColor = Color(0xFF0B1220), navSelected = Color(0xFF60A5FA),
    navUnselected = Color(0xFF64748B)
)

/** همه تم‌های موجود، به ترتیب نمایش در تنظیمات. */
val AllSkins: List<AppSkin> = listOf(
    MinimalDaySkin, MinimalNightSkin, SakuraSkin, VioletSkin, LotusSkin, OceanSkin, GoldSkin
)

fun skinOf(palette: Palette): AppSkin = when (palette) {
    Palette.SAKURA -> SakuraSkin
    Palette.VIOLET -> VioletSkin
    Palette.LOTUS -> LotusSkin
    Palette.OCEAN -> OceanSkin
    Palette.GOLD -> GoldSkin
    Palette.MINIMAL_DAY -> MinimalDaySkin
    Palette.MINIMAL_NIGHT -> MinimalNightSkin
}

val LocalAppSkin: ProvidableCompositionLocal<AppSkin> = compositionLocalOf { SakuraSkin }

@Suppress("UNUSED_PARAMETER")
@Composable
fun KharjYarTheme(
    themeMode: ThemeMode,
    palette: Palette,
    content: @Composable () -> Unit
) {
    // خانواده مینیمال واقعاً تطبیقی است: در حالت پیش‌فرض سیستم، شب/روز گوشی
    // مستقیماً نسخه شب/روز را انتخاب می‌کند. انتخاب روشن/تیره نیز آن را اجبار می‌کند.
    val wantsDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val effectivePalette = if (palette == Palette.MINIMAL_DAY || palette == Palette.MINIMAL_NIGHT) {
        if (wantsDark) Palette.MINIMAL_NIGHT else Palette.MINIMAL_DAY
    } else palette
    val skin = skinOf(effectivePalette)
    val colorScheme = when (effectivePalette) {
        Palette.SAKURA -> SakuraScheme
        Palette.VIOLET -> VioletScheme
        Palette.LOTUS -> LotusScheme
        Palette.OCEAN -> OceanScheme
        Palette.GOLD -> GoldScheme
        Palette.MINIMAL_DAY -> MinimalDayScheme
        Palette.MINIMAL_NIGHT -> MinimalNightScheme
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
