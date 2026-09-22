package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * نگهبان به‌روزرسانی خودکار ویجت.
 *
 * قبلاً هر صفحه‌ای که داده را تغییر می‌داد باید خودش WidgetUpdater را صدا می‌زد و
 * هر جا فراموش می‌شد، ویجت کهنه می‌ماند. حالا Application به جریان Room گوش می‌دهد.
 * این تست مطمئن می‌شود آن اتصال حذف نشود.
 */
class WidgetAutoUpdateTest {

    private fun source(path: String) = File(path).readText()

    @Test
    fun `application observes data and refreshes widget`() {
        val app = source("src/main/java/ir/kharjyar/app/KharjYarApp.kt")
        assertTrue("تابع رصد داده حذف شده", app.contains("observeDataForWidget"))
        assertTrue("رصد در onCreate صدا زده نمی‌شود", app.contains("observeDataForWidget()"))
        assertTrue("به تراکنش‌ها گوش نمی‌دهد", app.contains("txDao.observeAll()"))
        assertTrue("ویجت را تازه نمی‌کند", app.contains("WidgetUpdater.requestUpdate"))
    }

    @Test
    fun `midnight alarm keeps jalali date fresh`() {
        val widget = source("src/main/java/ir/kharjyar/app/widget/KharjYarWidget.kt")
        assertTrue("زنگ نیمه‌شب تعریف نشده", widget.contains("fun scheduleMidnight"))
        assertTrue("اکشن نیمه‌شب مدیریت نمی‌شود", widget.contains("ACTION_MIDNIGHT"))
        // زنگ باید هنگام افزودن ویجت هم چیده شود
        assertTrue("onEnabled زنگ را نمی‌چیند", widget.contains("override fun onEnabled"))
    }

    @Test
    fun `midnight action is declared in manifest`() {
        val manifest = source("src/main/AndroidManifest.xml")
        assertTrue(
            "اکشن نیمه‌شب در مانیفست ثبت نشده — زنگ به گیرنده نمی‌رسد",
            manifest.contains("ir.kharjyar.app.widget.MIDNIGHT")
        )
    }

    @Test
    fun `glow helpers exist for cards`() {
        val surfaces = source("src/main/java/ir/kharjyar/app/ui/components/Surfaces.kt")
        assertTrue("هاله پیرامون کارت حذف شده", surfaces.contains("fun Modifier.softGlow"))
        assertTrue("نور چرخان حذف شده", surfaces.contains("fun Modifier.orbitGlow"))
    }
}
