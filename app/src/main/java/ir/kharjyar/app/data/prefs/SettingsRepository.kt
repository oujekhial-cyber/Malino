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
/** تم بصری برنامه. فعلاً تنها تم: شفق قطبی. */
enum class Palette { AURORA, EMERALD, PAPER, PLUM, SLATE }
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
    val widgetShowClock: Boolean = true
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
            widgetShowClock = p[Keys.WIDGET_CLOCK] ?: true
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

    /** تنظیمات غیرحساس برای بکاپ. */
    suspend fun exportForBackup(): Map<String, String> {
        val s = current()
        return mapOf(
            "theme_mode" to s.themeMode.name,
            "palette" to s.palette.name,
            "money_unit" to s.moneyUnit.name,
            "widget_content" to s.widgetContent.name,
            "widget_numbers" to s.widgetShowNumbers.toString(),
            "widget_clock" to s.widgetShowClock.toString()
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
