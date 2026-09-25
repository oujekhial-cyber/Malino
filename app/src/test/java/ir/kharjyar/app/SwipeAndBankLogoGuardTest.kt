package ir.kharjyar.app

import ir.kharjyar.app.ui.components.bankAssetKey
import ir.kharjyar.app.ui.components.bankColorOf
import ir.kharjyar.app.ui.components.bankShortOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * نگهبان‌های «کشیدن ردیف» و «نشان بانک».
 *
 * بخشی از این تست‌ها روی متن سورس کار می‌کند، چون رفتار لمسی و ترسیم Compose
 * در تست واحد اجرا نمی‌شود ولی برگشتن به پیاده‌سازی قبلی (که ردیف را در حالت
 * کشیده‌شده گیر می‌انداخت) به‌سادگی ممکن است.
 */
class SwipeAndBankLogoGuardTest {

    private fun source(path: String) = File(path).readText()

    private val swipe = "src/main/java/ir/kharjyar/app/ui/components/SwipeActions.kt"
    private val surfaces = "src/main/java/ir/kharjyar/app/ui/components/Surfaces.kt"

    @Test
    fun `swipe row does not use dismiss box anymore`() {
        val s = source(swipe)
        // فقط کد بررسی می‌شود؛ متن توضیحات حق دارد از پیاده‌سازی قبلی اسم ببرد
        val code = s.lines().filterNot { it.trimStart().startsWith("*") || it.trimStart().startsWith("//") }
            .joinToString("\n")
        // SwipeToDismissBox ردیف را وقتی واقعاً حذف نمی‌شد باز نگه می‌داشت
        assertFalse("SwipeToDismissBox برگشته است", code.contains("SwipeToDismissBox("))
        assertFalse("حالت dismiss برگشته است", code.contains("rememberSwipeToDismissBoxState"))
        assertFalse("ایمپورت dismiss box مانده است", code.contains("import androidx.compose.material3.SwipeToDismissBox"))
        assertTrue("کشیدن دستی افقی حذف شده", s.contains("Orientation.Horizontal"))
        assertTrue("جابه‌جایی مطلق (بدون آینه شدن در RTL) حذف شده", s.contains("absoluteOffset"))
    }

    @Test
    fun `row always returns to its place after the gesture`() {
        val s = source(swipe)
        val stop = s.substringAfter("onDragStopped = {").substringBefore("}")
        assertTrue("بعد از رها کردن انگشت، کارت به جای خودش برنمی‌گردد", stop.contains("animateTo(0f"))
        // برگشت باید پیش از اجرای عمل باشد تا اگر ردیف از فهرست برداشته شد،
        // وضعیت باز روی ردیف بعدی (که جای همان اندیس را می‌گیرد) نماند.
        assertTrue(
            "برگشت کارت باید قبل از onDelete انجام شود",
            stop.indexOf("animateTo(0f") < stop.indexOf("onDelete()")
        )
    }

    @Test
    fun `swipe decision is based on distance not velocity`() {
        val s = source(swipe)
        // آستانه بر حسب مسافت (dp) است تا تکان افقی هنگام اسکرول عمودی حذف نکند
        assertTrue("آستانه مسافتی تعریف نشده", s.contains("SwipeTrigger"))
        val trigger = Regex("""SwipeTrigger = (\d+)\.dp""").find(s)?.groupValues?.get(1)?.toInt()
        assertNotNull("مقدار آستانه پیدا نشد", trigger)
        assertTrue("آستانه کشیدن خیلی کم است و تصادفی فعال می‌شود", trigger!! >= 64)
        assertFalse("تصمیم بر اساس سرعت برگشته است", s.contains("velocityThreshold"))
    }

    @Test
    fun `action strip is placed absolutely and is visible before the end of the swipe`() {
        val s = source(swipe)
        assertTrue(
            "چیدمان نوار عمل به LTR قفل نشده؛ راست و چپ در صفحه راست‌به‌چپ جابه‌جا می‌شود",
            s.contains("LocalLayoutDirection provides LayoutDirection.Ltr")
        )
        // شفافیت نوار با میزان کشیدن زیاد می‌شود، یعنی از همان ابتدا دیده می‌شود
        assertTrue("نوار عمل تدریجی ظاهر نمی‌شود", s.contains("alpha = (abs(offsetX.value) / trigger)"))
    }

    @Test
    fun `list enter animation is short and skipped while scrolling`() {
        val s = source(surfaces)
        assertTrue("ساعت ورود صفحه تعریف نشده", s.contains("LocalScreenEnterTime"))
        assertTrue("پوشش صفحه برای انیمیشن ورود نیست", s.contains("fun ScreenEnterAnimation"))
        val fade = Regex("""fadeIn\(tween\((\d+),""").find(s)?.groupValues?.get(1)?.toInt()
        assertNotNull("زمان محو شدن پیدا نشد", fade)
        assertTrue("انیمیشن ورود هنوز کند است", fade!! <= 200)
        val step = Regex("""index\.coerceIn\(0, \d+\)\) \* (\d+)""").find(s)?.groupValues?.get(1)?.toInt()
        assertNotNull("گام تأخیر ترتیبی پیدا نشد", step)
        assertTrue("تأخیر ترتیبی هنوز زیاد است", step!! <= 25)

        val dash = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        assertTrue("صفحه خانه ساعت ورود را تنظیم نمی‌کند", dash.contains("ScreenEnterAnimation {"))
    }

    @Test
    fun `every offered bank has its real logo file`() {
        val screen = source("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt")
        val listing = screen.substringAfter("private val bankNames = listOf(").substringBefore(")")
        val names = Regex("\"([^\"]+)\"").findAll(listing).map { it.groupValues[1] }.toList()
        assertTrue("فهرست بانک‌ها پیدا نشد", names.size > 20)
        for (name in names) {
            if (name == "سایر") continue
            val key = bankAssetKey(name)
            assertNotNull("لوگوی بانک «$name» تعریف نشده", key)
            val file = File("src/main/res/drawable-xxxhdpi/bank_$key.png")
            assertTrue("فایل لوگوی بانک «$name» ($key) نیست", file.isFile && file.length() > 0)
            assertNotNull("رنگ بانک «$name» تعریف نشده", bankColorOf(name))
        }
    }

    @Test
    fun `bank logo tolerates the word bank and unknown names`() {
        assertEquals("mellat", bankAssetKey("بانک ملت"))
        assertEquals("mellat", bankAssetKey("ملت"))
        assertEquals("melal", bankAssetKey("موسسه اعتباری ملل"))
        // «صادرات» و «توسعه صادرات» دو بانک جدا هستند
        assertEquals("saderat", bankAssetKey("بانک صادرات"))
        assertEquals("toseesaderat", bankAssetKey("بانک توسعه صادرات"))
        assertEquals("mehriran", bankAssetKey("قرض‌الحسنه مهر"))
        assertEquals("toseetaavon", bankAssetKey("توسعه تعاون"))
        // بانک ناشناس نباید برنامه را بشکند؛ فقط نشان ساده می‌گیرد
        assertEquals(null, bankAssetKey("بانک خیالی"))
        assertEquals(null, bankColorOf("بانک خیالی"))
        assertTrue(bankShortOf("بانک خیالی").isNotEmpty())
        assertTrue(bankShortOf("سایر").isNotEmpty())
    }

    @Test
    fun `bank mark is shown on the card and in the bank list`() {
        val card = source("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt")
        assertTrue("نشان بانک روی کارت حساب نیست", card.contains("BankLogo("))
        val accounts = source("src/main/java/ir/kharjyar/app/ui/screens/AccountsScreen.kt")
        assertTrue("نشان بانک در فهرست حساب‌ها نیست", accounts.contains("BankLogo("))
        val edit = source("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt")
        assertTrue("نشان بانک کنار نام بانک در فهرست انتخاب نیست", edit.contains("leadingOf = { BankLogo("))
        val logo = source("src/main/java/ir/kharjyar/app/ui/components/BankLogo.kt")
        assertTrue("لوگوی واقعی بانک‌ها استفاده نمی‌شود", logo.contains("painterResource"))
    }
}
