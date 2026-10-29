package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SmsHistoryCashAccountComboGuardTest {
    @Test fun `cash accounts appear in bank sms account combo`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
        assertTrue(source.contains("val activeAccounts=accounts.filter{!it.archived}"))
        assertTrue(source.contains("ComboBox(label=\"حساب\",options=activeAccounts"))
        assertTrue(source.contains("AccountType.CASH)\"\${account.title} • حساب نقدی\""))
        assertTrue(source.contains("bankInBody=selectedBank.isNotBlank()"))
    }
}
