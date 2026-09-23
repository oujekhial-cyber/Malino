package ir.kharjyar.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.kharjyar.app.core.money.MoneyUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
/** تم بصری برنامه. */
enum class Palette { SAKURA, INDIGO, VIOLET, LOTUS, MIDNIGHT, SUNSET, OCEAN, GOLD }
enum class WidgetContent { TODAY_EXPENSE, MONTH_EXPENSE, SUMMARY, RECENT }

/** پس‌زمینه ویجت: رنگ ساده تم یا تصویر شکوفه شب (هر دو با شیشه‌ای بودن قابل تنظیم). */
enum class WidgetBackground { THEME, SAKURA }

/** سبک نمایش ارقام در کل برنامه و ویجت. */
enum class DigitStyle { PERSIAN, LATIN }

/** تراز افقی متن‌ها در ویجت. */
/**
 * قالب آماده ویجت، بر پایه شش طرحی که کاربر انتخاب کرده است.
 * هر قالب چیدمان و اندازه‌های خودش را دارد و نیازی به تنظیم دستی نیست.
 */
enum class WidgetLayout {
    /** ۱ — لوکس: قاب طلایی، ساعت خیلی بزرگ، ردیف‌های جداشده با خط. */
    ROYAL,
    /** ۲ — مینیمال: عنوان بالا-راست، ساعت بزرگ سمت چپ. */
    MINIMAL,
    /** ۳ — کارتی: عنوان و ساعت در یک ردیف، مقادیر در دو نوار جدا. */
    PANELS,
    /** ۴ — ستونی: سربرگ کامل بالا، مقادیر در دو کارت بزرگ زیر آن. */
    STACKED,
    /** ۵ — دوبخشی: خط عمودی وسط، مقادیر راست و ساعت چپ. */
    SPLIT,
    /** ۶ — شیشه‌ای: مثل دوبخشی با قاب نورانی و تاکید بیشتر روی شفافیت. */
    GLASS
}

enum class WidgetAlign { START, CENTER, END }

/** جای عمودی متن‌های ویجت: بالا، وسط یا پایین. */
enum class WidgetVAlign { TOP, CENTER, BOTTOM }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: Palette = Palette.SAKURA,
    val moneyUnit: MoneyUnit = MoneyUnit.RIAL,
    val appLockEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 60,
    val onboardingDone: Boolean = false,
    val widgetContent: WidgetContent = WidgetContent.SUMMARY,
    /** قالب ظاهری ویجت. */
    val widgetLayout: WidgetLayout = WidgetLayout.SPLIT,
    val widgetShowNumbers: Boolean = true,
    /** اگر قفل فعال است، نمایش اعداد در ویجت باید صریحاً مجاز شود. */
    val widgetShowNumbersWhenLocked: Boolean = false,
    /** حساب پیش‌فرض داشبورد و ویجت. null یعنی «همه حساب‌ها». */
    val defaultAccountId: Long? = null,
    /** نمایش ساعت و تاریخ در سمت راست ویجت. */
    val widgetShowClock: Boolean = true,
    /** میزان شیشه‌ای/شفاف بودن پس‌زمینه ویجت: ۰ کاملاً شفاف تا ۱۰۰ کاملاً مات. */
    val widgetOpacity: Int = 92,
    /** اندازه فونت ساعت ویجت بر حسب sp. */
    val widgetClockSize: Int = 40,
    /** اندازه فونت تاریخ‌های ویجت بر حسب sp. */
    val widgetDateSize: Int = 13,
    /** اندازه فونت اعداد مالی ویجت بر حسب sp. */
    val widgetValueSize: Int = 14,
    /** اندازه فونت برچسب‌های ویجت بر حسب sp. */
    val widgetLabelSize: Int = 10,
    /** نوع پس‌زمینه ویجت. */
    val widgetBackground: WidgetBackground = WidgetBackground.THEME,
    /** نمایش تاریخ شمسی و میلادی زیر ساعت ویجت. */
    val widgetShowDates: Boolean = true,
    /** نمایش تصویر پس‌زمینه تم پشت ویجت. */
    val widgetShowImage: Boolean = true,
    /** جلوگیری از اسکرین‌شات و ضبط صفحه (FLAG_SECURE). */
    val secureScreen: Boolean = true,
    /** ارقام فارسی یا لاتین در کل برنامه و ویجت. */
    val digitStyle: DigitStyle = DigitStyle.PERSIAN,
    /** حرکت آرام هاله نور شیشه‌ای روی کارت‌های صفحه خانه. */
    val cardShine: Boolean = false,
    /** نمایش مبالغ در کارت خانه (با دکمه چشم عوض می‌شود و ماندگار است). */
    val amountsVisible: Boolean = true,
    /** چیدمان متن‌های ویجت. */
    val widgetTitleAlign: WidgetAlign = WidgetAlign.START,
    val widgetClockAlign: WidgetAlign = WidgetAlign.CENTER,
    /** جای عمودی ستون مقادیر مالی. */
    val widgetTitleVAlign: WidgetVAlign = WidgetVAlign.CENTER,
    /** جای عمودی ستون ساعت و تاریخ. */
    val widgetClockVAlign: WidgetVAlign = WidgetVAlign.CENTER,
    /** ترتیب عمودی پنل ساعت نسبت به تاریخ‌ها. */
    val widgetDatesBelowClock: Boolean = true,
    /** فاصله عمودی پنل ساعت از بالای ویجت (۰=وسط، منفی=بالاتر، مثبت=پایین‌تر) بر حسب dp. */
    val widgetClockOffsetY: Int = 0,
    /** فاصله عمودی متن‌های خرج‌یار بر حسب dp. */
    val widgetTitleOffsetY: Int = 0
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val PALETTE = stringPreferencesKey("palette")
        val MONEY_UNIT = stringPreferencesKey("money_unit")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val LOCK_TIMEOUT = intPreferencesKey("lock_timeout")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
        val WIDGET_CONTENT = stringPreferencesKey("widget_content")
        val WIDGET_NUMBERS = booleanPreferencesKey("widget_numbers")
        val WIDGET_NUMBERS_LOCKED = booleanPreferencesKey("widget_numbers_locked")
        val DEFAULT_ACCOUNT = longPreferencesKey("default_account_id")
        val WIDGET_CLOCK = booleanPreferencesKey("widget_clock")
        val WIDGET_OPACITY = intPreferencesKey("widget_opacity")
        val WIDGET_CLOCK_SIZE = intPreferencesKey("widget_clock_size")
        val WIDGET_DATE_SIZE = intPreferencesKey("widget_date_size")
        val WIDGET_VALUE_SIZE = intPreferencesKey("widget_value_size")
        val WIDGET_LABEL_SIZE = intPreferencesKey("widget_label_size")
        val WIDGET_BG = stringPreferencesKey("widget_background")
        val WIDGET_DATES = booleanPreferencesKey("widget_dates")
        val WIDGET_IMAGE = booleanPreferencesKey("widget_image")
        val SECURE_SCREEN = booleanPreferencesKey("secure_screen")
        val DIGIT_STYLE = stringPreferencesKey("digit_style")
        val CARD_SHINE = booleanPreferencesKey("card_shine")
        val AMOUNTS_VISIBLE = booleanPreferencesKey("amounts_visible")
        val W_TITLE_ALIGN = stringPreferencesKey("w_title_align")
        val W_CLOCK_ALIGN = stringPreferencesKey("w_clock_align")
        val W_LAYOUT = stringPreferencesKey("w_layout")
        val W_TITLE_VALIGN = stringPreferencesKey("w_title_valign")
        val W_CLOCK_VALIGN = stringPreferencesKey("w_clock_valign")
        val W_DATES_BELOW = booleanPreferencesKey("w_dates_below")
        val W_CLOCK_OFFSET_Y = intPreferencesKey("w_clock_offset_y")
        val W_TITLE_OFFSET_Y = intPreferencesKey("w_title_offset_y")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = enumOf(p[Keys.THEME], ThemeMode.SYSTEM),
            palette = paletteOf(p[Keys.PALETTE]),
            moneyUnit = enumOf(p[Keys.MONEY_UNIT], MoneyUnit.RIAL),
            appLockEnabled = p[Keys.APP_LOCK] ?: false,
            lockTimeoutSeconds = p[Keys.LOCK_TIMEOUT] ?: 60,
            onboardingDone = p[Keys.ONBOARDING] ?: false,
            widgetContent = enumOf(p[Keys.WIDGET_CONTENT], WidgetContent.SUMMARY),
            widgetLayout = enumOf(p[Keys.W_LAYOUT], WidgetLayout.SPLIT),
            widgetShowNumbers = p[Keys.WIDGET_NUMBERS] ?: true,
            widgetShowNumbersWhenLocked = p[Keys.WIDGET_NUMBERS_LOCKED] ?: false,
            defaultAccountId = p[Keys.DEFAULT_ACCOUNT]?.takeIf { it > 0 },
            widgetShowClock = p[Keys.WIDGET_CLOCK] ?: true,
            widgetOpacity = (p[Keys.WIDGET_OPACITY] ?: 92).coerceIn(0, 100),
            widgetClockSize = (p[Keys.WIDGET_CLOCK_SIZE] ?: 40).coerceIn(18, 72),
            widgetDateSize = (p[Keys.WIDGET_DATE_SIZE] ?: 13).coerceIn(8, 28),
            widgetValueSize = (p[Keys.WIDGET_VALUE_SIZE] ?: 14).coerceIn(9, 30),
            widgetLabelSize = (p[Keys.WIDGET_LABEL_SIZE] ?: 10).coerceIn(7, 22),
            widgetBackground = enumOf(p[Keys.WIDGET_BG], WidgetBackground.THEME),
            widgetShowDates = p[Keys.WIDGET_DATES] ?: true,
            widgetShowImage = p[Keys.WIDGET_IMAGE] ?: true,
            secureScreen = p[Keys.SECURE_SCREEN] ?: true,
            digitStyle = enumOf(p[Keys.DIGIT_STYLE], DigitStyle.PERSIAN).also {
                // پرچم سراسری ارقام همگام با تنظیم کاربر نگه داشته می‌شود
                ir.kharjyar.app.core.text.Digits.usePersianDigits = it == DigitStyle.PERSIAN
            },
            cardShine = p[Keys.CARD_SHINE] ?: false,
            amountsVisible = p[Keys.AMOUNTS_VISIBLE] ?: true,
            widgetTitleAlign = enumOf(p[Keys.W_TITLE_ALIGN], WidgetAlign.START),
            widgetClockAlign = enumOf(p[Keys.W_CLOCK_ALIGN], WidgetAlign.CENTER),
            widgetTitleVAlign = enumOf(p[Keys.W_TITLE_VALIGN], WidgetVAlign.CENTER),
            widgetClockVAlign = enumOf(p[Keys.W_CLOCK_VALIGN], WidgetVAlign.CENTER),
            widgetDatesBelowClock = p[Keys.W_DATES_BELOW] ?: true,
            widgetClockOffsetY = (p[Keys.W_CLOCK_OFFSET_Y] ?: 0).coerceIn(-40, 40),
            widgetTitleOffsetY = (p[Keys.W_TITLE_OFFSET_Y] ?: 0).coerceIn(-40, 40)
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setThemeMode(v: ThemeMode) = edit { it[Keys.THEME] = v.name }
    suspend fun setPalette(v: Palette) = edit { it[Keys.PALETTE] = v.name }
    suspend fun setMoneyUnit(v: MoneyUnit) = edit { it[Keys.MONEY_UNIT] = v.name }
    suspend fun setAppLock(v: Boolean) = edit { it[Keys.APP_LOCK] = v }
    suspend fun setLockTimeout(v: Int) = edit { it[Keys.LOCK_TIMEOUT] = v }
    suspend fun setOnboardingDone(v: Boolean) = edit { it[Keys.ONBOARDING] = v }
    suspend fun setWidgetContent(v: WidgetContent) = edit { it[Keys.WIDGET_CONTENT] = v.name }
    suspend fun setWidgetLayout(v: WidgetLayout) = edit { it[Keys.W_LAYOUT] = v.name }
    suspend fun setWidgetShowNumbers(v: Boolean) = edit { it[Keys.WIDGET_NUMBERS] = v }
    suspend fun setWidgetShowNumbersWhenLocked(v: Boolean) = edit { it[Keys.WIDGET_NUMBERS_LOCKED] = v }
    suspend fun setDefaultAccount(v: Long?) = edit { it[Keys.DEFAULT_ACCOUNT] = v ?: 0L }
    suspend fun setWidgetShowClock(v: Boolean) = edit { it[Keys.WIDGET_CLOCK] = v }
    suspend fun setWidgetOpacity(v: Int) = edit { it[Keys.WIDGET_OPACITY] = v.coerceIn(0, 100) }
    suspend fun setWidgetClockSize(v: Int) = edit { it[Keys.WIDGET_CLOCK_SIZE] = v.coerceIn(18, 72) }
    suspend fun setWidgetDateSize(v: Int) = edit { it[Keys.WIDGET_DATE_SIZE] = v.coerceIn(8, 28) }
    suspend fun setWidgetValueSize(v: Int) = edit { it[Keys.WIDGET_VALUE_SIZE] = v.coerceIn(9, 30) }
    suspend fun setWidgetLabelSize(v: Int) = edit { it[Keys.WIDGET_LABEL_SIZE] = v.coerceIn(7, 22) }
    suspend fun setWidgetBackground(v: WidgetBackground) = edit { it[Keys.WIDGET_BG] = v.name }
    suspend fun setWidgetShowDates(v: Boolean) = edit { it[Keys.WIDGET_DATES] = v }
    suspend fun setWidgetShowImage(v: Boolean) = edit { it[Keys.WIDGET_IMAGE] = v }
    suspend fun setSecureScreen(v: Boolean) = edit { it[Keys.SECURE_SCREEN] = v }
    suspend fun setDigitStyle(v: DigitStyle) = edit { it[Keys.DIGIT_STYLE] = v.name }
    suspend fun setCardShine(v: Boolean) = edit { it[Keys.CARD_SHINE] = v }
    suspend fun setAmountsVisible(v: Boolean) = edit { it[Keys.AMOUNTS_VISIBLE] = v }
    suspend fun setWidgetTitleAlign(v: WidgetAlign) = edit { it[Keys.W_TITLE_ALIGN] = v.name }
    suspend fun setWidgetClockAlign(v: WidgetAlign) = edit { it[Keys.W_CLOCK_ALIGN] = v.name }
    suspend fun setWidgetTitleVAlign(v: WidgetVAlign) = edit { it[Keys.W_TITLE_VALIGN] = v.name }
    suspend fun setWidgetClockVAlign(v: WidgetVAlign) = edit { it[Keys.W_CLOCK_VALIGN] = v.name }
    suspend fun setWidgetDatesBelowClock(v: Boolean) = edit { it[Keys.W_DATES_BELOW] = v }
    suspend fun setWidgetClockOffsetY(v: Int) = edit { it[Keys.W_CLOCK_OFFSET_Y] = v.coerceIn(-40, 40) }
    suspend fun setWidgetTitleOffsetY(v: Int) = edit { it[Keys.W_TITLE_OFFSET_Y] = v.coerceIn(-40, 40) }

    /** تنظیمات غیرحساس برای بکاپ. */
    suspend fun exportForBackup(): Map<String, String> {
        val s = current()
        return mapOf(
            "theme_mode" to s.themeMode.name,
            "palette" to s.palette.name,
            "money_unit" to s.moneyUnit.name,
            "widget_content" to s.widgetContent.name,
            "w_layout" to s.widgetLayout.name,
            "widget_numbers" to s.widgetShowNumbers.toString(),
            "widget_clock" to s.widgetShowClock.toString(),
            "widget_opacity" to s.widgetOpacity.toString(),
            "widget_clock_size" to s.widgetClockSize.toString(),
            "widget_date_size" to s.widgetDateSize.toString(),
            "widget_value_size" to s.widgetValueSize.toString(),
            "widget_label_size" to s.widgetLabelSize.toString(),
            "widget_background" to s.widgetBackground.name,
            "widget_dates" to s.widgetShowDates.toString(),
            "widget_image" to s.widgetShowImage.toString(),
            "secure_screen" to s.secureScreen.toString(),
            "digit_style" to s.digitStyle.name,
            "card_shine" to s.cardShine.toString()
        )
    }

    suspend fun importFromBackup(map: Map<String, String>) {
        context.dataStore.edit { p ->
            map["theme_mode"]?.let { v -> runCatching { ThemeMode.valueOf(v) }.getOrNull()?.let { p[Keys.THEME] = it.name } }
            map["palette"]?.let { v -> p[Keys.PALETTE] = paletteOf(v).name }
            map["money_unit"]?.let { v -> runCatching { MoneyUnit.valueOf(v) }.getOrNull()?.let { p[Keys.MONEY_UNIT] = it.name } }
            map["widget_content"]?.let { v -> runCatching { WidgetContent.valueOf(v) }.getOrNull()?.let { p[Keys.WIDGET_CONTENT] = it.name } }
            map["w_layout"]?.let { v -> runCatching { WidgetLayout.valueOf(v) }.getOrNull()?.let { p[Keys.W_LAYOUT] = it.name } }
            map["widget_numbers"]?.let { p[Keys.WIDGET_NUMBERS] = it.toBoolean() }
            map["widget_clock"]?.let { p[Keys.WIDGET_CLOCK] = it.toBoolean() }
            map["widget_opacity"]?.toIntOrNull()?.let { p[Keys.WIDGET_OPACITY] = it.coerceIn(0, 100) }
            map["widget_clock_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_CLOCK_SIZE] = it.coerceIn(18, 72) }
            map["widget_date_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_DATE_SIZE] = it.coerceIn(8, 28) }
            map["widget_value_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_VALUE_SIZE] = it.coerceIn(9, 30) }
            map["widget_label_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_LABEL_SIZE] = it.coerceIn(7, 22) }
            map["widget_background"]?.let { v -> runCatching { WidgetBackground.valueOf(v) }.getOrNull()?.let { p[Keys.WIDGET_BG] = it.name } }
            map["widget_dates"]?.let { p[Keys.WIDGET_DATES] = it.toBoolean() }
            map["widget_image"]?.let { p[Keys.WIDGET_IMAGE] = it.toBoolean() }
            map["secure_screen"]?.let { p[Keys.SECURE_SCREEN] = it.toBoolean() }
            map["digit_style"]?.let { v -> runCatching { DigitStyle.valueOf(v) }.getOrNull()?.let { p[Keys.DIGIT_STYLE] = it.name } }
            map["card_shine"]?.let { p[Keys.CARD_SHINE] = it.toBoolean() }
        }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    /** نگاشت پالت‌های قدیمی (اقیانوس/جنگل/…) به تم‌های جدید. */
    private fun paletteOf(name: String?): Palette =
        name?.let { runCatching { Palette.valueOf(it) }.getOrNull() } ?: Palette.SAKURA

    private inline fun <reified T : Enum<T>> enumOf(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
