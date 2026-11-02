package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountListSettingsPlacementGuardTest {
    @Test
    fun `settings root only links to account management and accounts are listed inside that page`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
        val root = source.substring(
            source.indexOf("if (section == null)"),
            source.indexOf("// ---------- تم ----------")
        )
        val accountPage = source.substring(
            source.indexOf("if (section == \"account\")"),
            source.indexOf("// ---------- پول ----------")
        )

        assertTrue(root.contains("SettingsMenuRow(\"مدیریت حساب\")"))
        assertFalse(root.contains("حساب‌های معرفی‌شده"))
        assertFalse(root.contains("accounts.forEach"))

        val selector = accountPage.indexOf("حساب پیش‌فرض داشبورد و ویجت")
        val list = accountPage.indexOf("حساب‌های معرفی‌شده")
        assertTrue(selector >= 0 && list > selector)
        listOf(
            "accounts.forEach",
            "BankLogo(",
            "account.title",
            "account.maskedNumber",
            "accountEdit/\${account.id}",
            "افزودن حساب جدید",
            "accountEdit/0"
        ).forEach { assertTrue(it, accountPage.contains(it)) }
        assertFalse(accountPage.contains("NavRow(\"لیست حساب‌ها\")"))
    }
}
