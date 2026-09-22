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
enum class Palette { AURORA, EMERALD, PAPER, PLUM, SLATE, GOLD, SAKURA, OCEAN }
enum class WidgetContent { TODAY_EXPENSE, MONTH_EXPENSE, SUMMARY, RECENT }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: Palette = Palette.AURORA,
    val moneyUnit: MoneyUnit = MoneyUnit.TOMAN,
    val appLockEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 60,
    val onboardingDone: Boolean = false,
    val widgetContent: WidgetContent = WidgetContent.SUMMARY,
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
    val widgetLabelSize: Int = 10
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
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = enumOf(p[Keys.THEME], ThemeMode.SYSTEM),
            palette = paletteOf(p[Keys.PALETTE]),
            moneyUnit = enumOf(p[Keys.MONEY_UNIT], MoneyUnit.TOMAN),
            appLockEnabled = p[Keys.APP_LOCK] ?: false,
            lockTimeoutSeconds = p[Keys.LOCK_TIMEOUT] ?: 60,
            onboardingDone = p[Keys.ONBOARDING] ?: false,
            widgetContent = enumOf(p[Keys.WIDGET_CONTENT], WidgetContent.SUMMARY),
            widgetShowNumbers = p[Keys.WIDGET_NUMBERS] ?: true,
            widgetShowNumbersWhenLocked = p[Keys.WIDGET_NUMBERS_LOCKED] ?: false,
            defaultAccountId = p[Keys.DEFAULT_ACCOUNT]?.takeIf { it > 0 },
            widgetShowClock = p[Keys.WIDGET_CLOCK] ?: true,
            widgetOpacity = (p[Keys.WIDGET_OPACITY] ?: 92).coerceIn(0, 100),
            widgetClockSize = (p[Keys.WIDGET_CLOCK_SIZE] ?: 40).coerceIn(18, 72),
            widgetDateSize = (p[Keys.WIDGET_DATE_SIZE] ?: 13).coerceIn(8, 28),
            widgetValueSize = (p[Keys.WIDGET_VALUE_SIZE] ?: 14).coerceIn(9, 30),
            widgetLabelSize = (p[Keys.WIDGET_LABEL_SIZE] ?: 10).coerceIn(7, 22)
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
    suspend fun setWidgetShowNumbers(v: Boolean) = edit { it[Keys.WIDGET_NUMBERS] = v }
    suspend fun setWidgetShowNumbersWhenLocked(v: Boolean) = edit { it[Keys.WIDGET_NUMBERS_LOCKED] = v }
    suspend fun setDefaultAccount(v: Long?) = edit { it[Keys.DEFAULT_ACCOUNT] = v ?: 0L }
    suspend fun setWidgetShowClock(v: Boolean) = edit { it[Keys.WIDGET_CLOCK] = v }
    suspend fun setWidgetOpacity(v: Int) = edit { it[Keys.WIDGET_OPACITY] = v.coerceIn(0, 100) }
    suspend fun setWidgetClockSize(v: Int) = edit { it[Keys.WIDGET_CLOCK_SIZE] = v.coerceIn(18, 72) }
    suspend fun setWidgetDateSize(v: Int) = edit { it[Keys.WIDGET_DATE_SIZE] = v.coerceIn(8, 28) }
    suspend fun setWidgetValueSize(v: Int) = edit { it[Keys.WIDGET_VALUE_SIZE] = v.coerceIn(9, 30) }
    suspend fun setWidgetLabelSize(v: Int) = edit { it[Keys.WIDGET_LABEL_SIZE] = v.coerceIn(7, 22) }

    /** تنظیمات غیرحساس برای بکاپ. */
    suspend fun exportForBackup(): Map<String, String> {
        val s = current()
        return mapOf(
            "theme_mode" to s.themeMode.name,
            "palette" to s.palette.name,
            "money_unit" to s.moneyUnit.name,
            "widget_content" to s.widgetContent.name,
            "widget_numbers" to s.widgetShowNumbers.toString(),
            "widget_clock" to s.widgetShowClock.toString(),
            "widget_opacity" to s.widgetOpacity.toString(),
            "widget_clock_size" to s.widgetClockSize.toString(),
            "widget_date_size" to s.widgetDateSize.toString(),
            "widget_value_size" to s.widgetValueSize.toString(),
            "widget_label_size" to s.widgetLabelSize.toString()
        )
    }

    suspend fun importFromBackup(map: Map<String, String>) {
        context.dataStore.edit { p ->
            map["theme_mode"]?.let { v -> runCatching { ThemeMode.valueOf(v) }.getOrNull()?.let { p[Keys.THEME] = it.name } }
            map["palette"]?.let { v -> p[Keys.PALETTE] = paletteOf(v).name }
            map["money_unit"]?.let { v -> runCatching { MoneyUnit.valueOf(v) }.getOrNull()?.let { p[Keys.MONEY_UNIT] = it.name } }
            map["widget_content"]?.let { v -> runCatching { WidgetContent.valueOf(v) }.getOrNull()?.let { p[Keys.WIDGET_CONTENT] = it.name } }
            map["widget_numbers"]?.let { p[Keys.WIDGET_NUMBERS] = it.toBoolean() }
            map["widget_clock"]?.let { p[Keys.WIDGET_CLOCK] = it.toBoolean() }
            map["widget_opacity"]?.toIntOrNull()?.let { p[Keys.WIDGET_OPACITY] = it.coerceIn(0, 100) }
            map["widget_clock_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_CLOCK_SIZE] = it.coerceIn(18, 72) }
            map["widget_date_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_DATE_SIZE] = it.coerceIn(8, 28) }
            map["widget_value_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_VALUE_SIZE] = it.coerceIn(9, 30) }
            map["widget_label_size"]?.toIntOrNull()?.let { p[Keys.WIDGET_LABEL_SIZE] = it.coerceIn(7, 22) }
        }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    /** نگاشت پالت‌های قدیمی (اقیانوس/جنگل/…) به تم‌های جدید. */
    private fun paletteOf(name: String?): Palette =
        name?.let { runCatching { Palette.valueOf(it) }.getOrNull() } ?: Palette.AURORA

    private inline fun <reified T : Enum<T>> enumOf(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
}
