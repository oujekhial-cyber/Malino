package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LatestUiRoundGuardTest {
    private fun source(path: String) = File(path).readText()

    @Test fun `review is named bank messages`() {
        val root = source("src/main/java/ir/kharjyar/app/ui/AppRoot.kt")
        assertTrue(root.contains("DrawerEntry(\"review\", \"پیامک‌های بانکی\""))
    }

    @Test fun `quick analysis hides keyboard and has one requested sample`() {
        val q = source("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt")
        assertTrue(q.contains("keyboard?.hide()"))
        assertTrue(q.contains("focusManager.clearFocus"))
        assertTrue(q.contains("امروز ۲۵۰ هزار تومن کیک از سوپرمارکت با حساب بانک ملی خریدم"))
        assertFalse(q.contains("دیروز ۸۰۰ تومن بنزین زدم"))
        assertFalse(q.contains("حقوق این ماه ۲۵ میلیون تومن واریز شد"))
    }

    @Test fun `home summary chips open filtered transaction screen`() {
        val d = source("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt")
        assertTrue(d.contains("nav.navigate(\"transactions/deposit\")"))
        assertTrue(d.contains("nav.navigate(\"transactions/withdraw\")"))
        val t = source("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt")
        assertTrue(t.contains("filterDirection"))
        assertTrue(t.contains("tx.direction == filterDirection"))
    }

    @Test fun `backup encryption defaults off`() {
        val b = source("src/main/java/ir/kharjyar/app/ui/screens/BackupScreen.kt")
        assertTrue(b.contains("var encrypt by remember { mutableStateOf(false) }"))
    }

    @Test fun `categories have professional header and expanded defaults`() {
        val c = source("src/main/java/ir/kharjyar/app/ui/screens/CategoriesScreen.kt")
        assertTrue(c.contains("دسته‌بندی هوشمند"))
        assertTrue(c.contains("دسته‌های پیشنهادی"))
        val db = source("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt")
        assertTrue(db.contains("حمل‌ونقل (خودرو)"))
        assertTrue(db.contains("سود و سرمایه‌گذاری"))
    }

    @Test fun `transaction filters use a dedicated professional panel`() {
        val t = source("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt")
        assertTrue(t.contains("فیلتر تراکنش‌ها"))
        assertTrue(t.contains("ProfessionalFilterChip"))
        assertTrue(t.contains("پاک کردن"))
    }

    @Test fun `internal transfers create two linked rows without category`() {
        val repo = source("src/main/java/ir/kharjyar/app/data/Repository.kt")
        assertTrue(repo.contains("suspend fun addInternalTransfer("))
        assertTrue(repo.contains("incomplete = false"))
        assertTrue(repo.contains("direction = TxDirection.DEPOSIT"))
        assertTrue(repo.contains("direction = TxDirection.WITHDRAW"))
        assertTrue(repo.contains("categoryId = null"))
        val manual = source("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt")
        assertTrue(manual.contains("حساب دیگر خودم در خرج‌یار"))
        assertTrue(manual.contains("حساب شخص دیگر"))
    }

    @Test fun `transaction rows show bank logo beside account`() {
        val tx = source("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt")
        assertTrue(tx.contains("BankLogo(bankName = acc.bankName"))
        assertFalse("رنگ حساب نباید کنار نام حساب باشد", tx.contains("Color(acc.colorArgb)"))
    }

    @Test fun `minimal day and night themes follow system mode`() {
        val prefs = source("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt")
        assertTrue(prefs.contains("MINIMAL_DAY"))
        assertTrue(prefs.contains("MINIMAL_NIGHT"))
        val theme = source("src/main/java/ir/kharjyar/app/ui/theme/Theme.kt")
        assertTrue(theme.contains("isSystemInDarkTheme()"))
        assertTrue(theme.contains("if (wantsDark) Palette.MINIMAL_NIGHT else Palette.MINIMAL_DAY"))
        val settings = source("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt")
        assertTrue(settings.contains("پیش‌فرض سیستم (تغییر خودکار روز/شب)"))
    }

    @Test fun `transaction parser does not hardcode any bank name`() {
        val parser = source("src/main/java/ir/kharjyar/app/core/nlp/TransactionParser.kt")
        val bankNames = listOf("توسعه تعاون", "ملی", "ملت", "صادرات", "تجارت", "سپه", "پاسارگاد")
        bankNames.forEach { bank ->
            assertFalse("نام بانک $bank داخل منطق Parser ثابت شده است", parser.contains("\"$bank\""))
        }
        assertTrue("Parser باید حساب‌های خود کاربر را ورودی بگیرد", parser.contains("accounts: List<ParserAccount>"))
    }
}
