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
}
