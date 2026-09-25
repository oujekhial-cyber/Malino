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
        // فهرست باید همه بانک‌ها و مؤسسه‌های اعتباری عضو شتاب را داشته باشد
        assertTrue("فهرست بانک‌ها کوتاه شده است", names.size >= 35)
        for (name in names) {
            if (name == "سایر") continue
            val key = bankAssetKey(name)
            assertNotNull("لوگوی بانک «$name» تعریف نشده", key)
            val png = File("src/main/res/drawable-xxxhdpi/bank_$key.png")
            val xml = File("src/main/res/drawable/bank_$key.xml")
            assertTrue(
                "فایل لوگوی بانک «$name» ($key) نیست",
                (png.isFile && png.length() > 0) || (xml.isFile && xml.length() > 0)
            )
            assertNotNull("رنگ بانک «$name» تعریف نشده", bankColorOf(name))
        }
    }

    @Test
    fun `card color follows the bank logo and is not chosen by hand`() {
        val edit = source("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt")
        assertFalse("انتخاب دستی رنگ کارت هنوز در فرم است", edit.contains("ColorPickerField("))
        assertTrue("رنگ کارت از روی بانک تنظیم نمی‌شود", edit.contains("bankCardColorArgb("))
        val card = source("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt")
        assertTrue("رنگ کارت از روی بانک گرفته نمی‌شود", card.contains("bankCardColor(bankName, colorArgb)"))
        // رنگ هر بانک باید با لوگوی خودش فرق داشته باشد، نه یک رنگ ثابت
        val distinct = listOf("ملت", "ملی", "پاسارگاد", "سامان", "شهر").mapNotNull { bankColorOf(it) }.distinct()
        assertEquals(5, distinct.size)
    }

    @Test
    fun `bank logo is watermarked on the account card`() {
        val card = source("src/main/java/ir/kharjyar/app/ui/components/BankCard.kt")
        assertTrue("واترمارک لوگو روی کارت نیست", card.contains("bankLogoRes(bankName)"))
        assertTrue("واترمارک باید کم‌رنگ باشد", Regex("alpha\\(0\\.(0|1)\\d*f\\)").containsMatchIn(card))
    }

    @Test
    fun `card scanner reads vertical cards`() {
        val scanner = source("src/main/java/ir/kharjyar/app/ui/components/CardScanner.kt")
        assertTrue("حالت کارت عمودی نیست", scanner.contains("verticalCard"))
        assertTrue("فریم‌ها با چرخش‌های مختلف خوانده نمی‌شوند", scanner.contains("rotationCycle"))
        val parser = source("src/main/java/ir/kharjyar/app/core/card/CardScanParser.kt")
        assertTrue("شماره کارت چندسطری پشتیبانی نمی‌شود", parser.contains("cardFromGroups"))
    }

    @Test
    fun `home screen drops the chart and the old quick add wording`() {
        val dash = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        assertFalse("نمودار هنوز در صفحه خانه است", dash.contains("LineChart("))
        assertFalse("متن قدیمی میان‌بر مانده است", dash.contains("بگو تا بنویسم"))
        assertFalse("مثال «۲۵۰ هزار تومن نان» مانده است", dash.contains("۲۵۰ هزار تومن نان"))
        assertTrue("میان‌بر «ثبت سریع» نیست", dash.contains("\"ثبت سریع\""))
        // نمودار باید فقط در صفحه گزارش بماند
        val reports = source("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt")
        assertTrue("نمودار از گزارش‌ها هم حذف شده", reports.contains("LineChart("))
    }

    @Test
    fun `home and transactions do not add the system insets twice`() {
        for (path in listOf(
            "src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt",
            "src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt"
        )) {
            assertTrue(
                "$path فاصله نوارهای سیستم را دوباره اضافه می‌کند",
                source(path).contains("contentWindowInsets = WindowInsets(0, 0, 0, 0)")
            )
        }
    }

    @Test
    fun `widget keeps asking to be pinned until it is on the home screen`() {
        val root = source("src/main/java/ir/kharjyar/app/ui/AppRoot.kt")
        assertTrue("درخواست چسباندن ویجت نیست", root.contains("requestPinAppWidget"))
        assertTrue("وجود ویجت بررسی نمی‌شود", root.contains("getAppWidgetIds(component).isNotEmpty()"))
        // دیگر نباید با یک پرچم برای همیشه خاموش شود
        assertFalse(
            "درخواست ویجت هنوز فقط یک‌بار انجام می‌شود",
            root.contains("if (settings.widgetAutoPinned) return@LaunchedEffect")
        )
    }

    @Test
    fun `entry form speaks about the amount, not the direction`() {
        val manual = source("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt")
        assertTrue(manual.contains("ثبت مبلغی که به حساب واریز شده است"))
        assertTrue(manual.contains("ثبت مبلغی که از حساب برداشت شده است"))
        assertFalse("متن قدیمی سربرگ مانده است", manual.contains("پول وارد حساب شد"))
        // برچسب توضیح برای هر عملیات جداگانه است
        assertTrue(manual.contains("واریز بابت چه بود؟"))
        assertTrue(manual.contains("خرید بابت چه بود؟"))
        assertTrue(manual.contains("انتقال بابت چه بود؟"))
        assertFalse(manual.contains("خرید/واریز بابت چی بوده؟"))
    }

    @Test
    fun `transfer is picked upfront instead of asking what the money was`() {
        val root = source("src/main/java/ir/kharjyar/app/ui/AppRoot.kt")
        assertTrue("گزینه انتقال وجه در پنجره نیست", root.contains("انتقال وجه"))
        assertTrue("عنوان پنجره «انتخاب عملیات» نیست", root.contains("\"انتخاب عملیات\""))
        assertFalse("متن اضافه پنجره مانده است", root.contains("چه چیزی ثبت کنیم؟"))
        assertFalse("زیرنویس «پول گرفتم» مانده است", root.contains("پول گرفتم"))
        assertFalse("زیرنویس «پول دادم» مانده است", root.contains("پول دادم"))
        assertTrue("مسیر انتقال وجه نیست", root.contains("presetTransfer = dir == \"transfer\""))

        val manual = source("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt")
        // در حالت انتخاب‌شده، دیگر پرسش «این پول چه بود؟» نمایش داده نمی‌شود
        assertTrue(manual.contains("presetTransfer"))
        assertTrue(manual.contains("TransferDirectionPicker"))
    }

    @Test
    fun `entry form starts at today and now`() {
        val manual = source("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt")
        assertTrue("ساعت پیش‌فرض همین لحظه نیست", manual.contains("PersianDate.nowHourMinute()"))
        assertFalse("ساعت ثابت ۱۲ هنوز پیش‌فرض است", manual.contains("mutableStateOf(12)"))
        val (h, m) = ir.kharjyar.app.core.date.PersianDate.nowHourMinute()
        assertTrue(h in 0..23)
        assertTrue(m in 0..59)
    }

    @Test
    fun `quick add shortcut shows a microphone`() {
        val manual = source("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt")
        assertTrue("آیکون میکروفون روی کادر ثبت سریع نیست", manual.contains("Icons.Filled.Mic"))
    }

    @Test
    fun `category picker can create a new category`() {
        val form = source("src/main/java/ir/kharjyar/app/ui/screens/TxForm.kt")
        assertTrue("گزینه دسته‌بندی جدید نیست", form.contains("+ دسته‌بندی جدید"))
        assertTrue("امکان ساخت دسته تازه نیست", form.contains("onCreate"))
        for (screen in listOf("ManualEntryScreen", "TransactionEditScreen")) {
            assertTrue(
                "$screen دسته‌بندی جدید را وصل نکرده",
                source("src/main/java/ir/kharjyar/app/ui/screens/$screen.kt").contains("onCreate = { name ->")
            )
        }
    }

    @Test
    fun `deposit and withdraw chips open the matching transaction list`() {
        val dash = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        assertTrue("میان‌بر واریز نیست", dash.contains("nav.navigate(\"transactions/deposit\")"))
        assertTrue("میان‌بر برداشت نیست", dash.contains("nav.navigate(\"transactions/withdraw\")"))
        val tx = source("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt")
        assertTrue("فیلتر جهت بانکی نیست", tx.contains("filterDirection"))
        assertTrue("انتقال‌ها بر اساس جهت فیلتر نمی‌شوند", tx.contains("tx.direction == filterDirection"))
    }

    @Test
    fun `tapping elsewhere clears the deposit withdraw filter`() {
        val dash = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        assertTrue("کلیک بیرون فیلتر را برنمی‌دارد", dash.contains("detectTapGestures { recentFilter = 0 }"))
        assertTrue(
            "انتخاب کارت فیلتر را برنمی‌دارد",
            dash.contains("و برداشتن فیلتر واریز/برداشت")
        )
        // فیلتر بر اساس جهت است، پس انتقال‌ها هم بسته به جهتشان می‌آیند
        assertTrue(dash.contains("it.direction == ir.kharjyar.app.data.db.TxDirection.DEPOSIT"))
        assertTrue(dash.contains("it.direction == ir.kharjyar.app.data.db.TxDirection.WITHDRAW"))
    }

    @Test
    fun `copy toast sits above the add button`() {
        val dash = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        val host = dash.substringAfter("snackbarHost = {").substringBefore("},")
        assertTrue("پیام کوتاه پشت دکمه ثبت می‌ماند", Regex("padding\\(bottom = \\d{2,}\\.dp\\)").containsMatchIn(host))
    }

    @Test
    fun `sms detection does not depend on persian or arabic spelling`() {
        val digits = source("src/main/java/ir/kharjyar/app/core/text/Digits.kt")
        assertTrue("نرمال‌سازی حروف عربی نیست", digits.contains("fun normalizeForMatch("))
        for (path in listOf(
            "src/main/java/ir/kharjyar/app/core/sms/Extraction.kt",
            "src/main/java/ir/kharjyar/app/core/sms/SmsClassifier.kt",
            "src/main/java/ir/kharjyar/app/core/sms/AccountMatcher.kt"
        )) {
            assertTrue("$path از نرمال‌سازی حروف استفاده نمی‌کند", source(path).contains("normalizeForMatch("))
        }
    }

    @Test
    fun `learned templates are reused and pending messages are retried`() {
        val repo = source("src/main/java/ir/kharjyar/app/data/Repository.kt")
        assertTrue("قالب‌های سایر سرشماره‌ها امتحان نمی‌شوند", repo.contains("templateDao.allEnabled()"))
        assertTrue("پردازش دوباره صف وجود ندارد", repo.contains("suspend fun reprocessPending()"))
        val train = source("src/main/java/ir/kharjyar/app/ui/screens/TemplateTrainScreen.kt")
        assertTrue("بعد از آموزش، صف دوباره پردازش نمی‌شود", train.contains("reprocessPending()"))
        assertTrue("فرم آموزش از تشخیص خودکار پر نمی‌شود", train.contains("Extractor.autoExtract(found.body)"))
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
