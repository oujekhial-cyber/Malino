package ir.kharjyar.app

import ir.kharjyar.app.data.prefs.AppSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * نگهبان‌های دور «ویجت و ورود اولیه».
 *
 * این تست‌ها روی متن سورس کار می‌کنند، چون رفتار آن‌ها (RemoteViews، لانچر و
 * پنجره مجوز سیستمی) در تست واحد قابل اجرا نیست ولی حذف شدنشان به‌راحتی
 * می‌تواند بی‌سر و صدا اتفاق بیفتد.
 */
class WidgetOptionsGuardTest {

    private fun source(path: String) = File(path).readText()

    @Test
    fun `widget row icon follows the direction of its own line`() {
        val widget = source("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt")
        assertTrue("مدل ردیف ویجت حذف شده", widget.contains("data class WidgetLine"))
        assertTrue("آیکون ردیف‌ها بر اساس جهت تنظیم نمی‌شود", widget.contains("fun applyRowIcons"))
        assertTrue("آیکون واریز استفاده نمی‌شود", widget.contains("R.drawable.w_ic_up"))
        assertTrue("آیکون برداشت استفاده نمی‌شود", widget.contains("R.drawable.w_ic_down"))
        // آیکون‌ها دیگر نباید ثابت و بدون توجه به ردیف رنگ شوند
        assertFalse(
            "رنگ‌آمیزی ثابت آیکون‌ها برگشته است",
            widget.contains("views.setInt(R.id.w_icon_1, \"setColorFilter\", skin.incomeColor.toArgb())")
        )
    }

    @Test
    fun `optional widget parts are applied`() {
        val widget = source("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt")
        assertTrue("گزینه‌های اختیاری ویجت اعمال نمی‌شوند", widget.contains("fun applyOptions"))
        assertTrue("نمایش عنوان اختیاری نیست", widget.contains("widgetShowTitle"))
        assertTrue("نمایش ساعت اختیاری نیست", widget.contains("widgetShowClock"))
        assertTrue("نمایش تاریخ‌ها اختیاری نیست", widget.contains("widgetShowDates"))
        assertTrue("اندازه فونت‌ها اعمال نمی‌شود", widget.contains("setTextViewTextSize"))
    }

    @Test
    fun `widget settings live on their own screen`() {
        val screen = source("src/main/java/ir/kharjyar/app/ui/screens/WidgetSettingsScreen.kt")
        assertTrue("پیش‌نمایش در صفحه تنظیمات ویجت نیست", screen.contains("WidgetPreview("))
        assertTrue("دکمه افزودن ویجت نیست", screen.contains("requestPinAppWidget"))

        val settings = source("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt")
        assertTrue("ورودی صفحه تنظیمات ویجت در تنظیمات نیست", settings.contains("widgetSettings"))
        // تنظیمات ویجت نباید دوباره داخل صفحه تنظیمات تکرار شود
        assertFalse("اسلایدر ویجت هنوز در صفحه تنظیمات است", settings.contains("میزان شیشه‌ای بودن"))

        val root = source("src/main/java/ir/kharjyar/app/ui/AppRoot.kt")
        assertTrue("مسیر صفحه تنظیمات ویجت ثبت نشده", root.contains("composable(\"widgetSettings\")"))
    }

    @Test
    fun `support section keeps email and drops phone number`() {
        val settings = source("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt")
        assertTrue("ایمیل پشتیبانی حذف شده", settings.contains("fasasoftrrr@gmail.com"))
        assertFalse("شماره تماس هنوز در تنظیمات هست", settings.contains("09399874951"))
        assertFalse("شماره‌گیر هنوز باز می‌شود", settings.contains("ACTION_DIAL"))
    }

    @Test
    fun `onboarding enters the app automatically`() {
        val onboarding = source("src/main/java/ir/kharjyar/app/ui/screens/OnboardingScreen.kt")
        assertTrue("ورود خودکار پیاده نشده", onboarding.contains("setOnboardingDone(true)"))
        assertTrue("پاسخ مجوز پیامک دنبال نمی‌شود", onboarding.contains("smsAnswered"))
        assertTrue("پاسخ مجوز اعلان دنبال نمی‌شود", onboarding.contains("notifAnswered"))
        assertFalse("دکمه «شروع» هنوز هست", onboarding.contains("Text(\"شروع\")"))
    }

    @Test
    fun `widget is offered once on first entry`() {
        val root = source("src/main/java/ir/kharjyar/app/ui/AppRoot.kt")
        assertTrue("افزودن خودکار ویجت حذف شده", root.contains("AutoPinWidget"))
        assertTrue("درخواست چسباندن ویجت انجام نمی‌شود", root.contains("requestPinAppWidget"))
        assertTrue("عرض کامل درخواست نمی‌شود", root.contains("OPTION_APPWIDGET_MIN_WIDTH"))
        assertTrue("قالب لوکس انتخاب نمی‌شود", root.contains("WidgetLayout.ROYAL"))
        assertTrue("فقط یک‌بار بودنِ کار تضمین نشده", root.contains("setWidgetAutoPinned(true)"))
    }

    @Test
    fun `home carousel snaps one card at a time`() {
        val dash = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        assertTrue("چسبیدن کارت‌ها فعال نیست", dash.contains("rememberSnapFlingBehavior"))
        assertTrue("پهنای ثابت صفحه کارت تعریف نشده", dash.contains("pageWidth"))
    }

    @Test
    fun `new widget prefs have sane defaults`() {
        val s = AppSettings()
        assertTrue("عنوان ویجت باید پیش‌فرض روشن باشد", s.widgetShowTitle)
        assertFalse("پیشنهاد خودکار ویجت نباید از قبل انجام‌شده باشد", s.widgetAutoPinned)
    }
}
