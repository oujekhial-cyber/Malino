package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiAccountPickerPriorityGuardTest {
    private val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()

    @Test fun `missing account opens an explicit popup and applies selection`() {
        assertTrue(quick.contains("showAccountPicker = result.accountId == null"))
        assertTrue(quick.contains("AlertDialog("))
        assertTrue(quick.contains("واریز به کدام حساب؟"))
        assertTrue(quick.contains("برداشت از کدام حساب؟"))
        assertTrue(quick.contains("accountId=account.id"))
        assertTrue(quick.contains("accountTitle=account.title"))
    }

    @Test fun `online AI analysis is visually before local fallback`() {
        val ai=quick.indexOf("تشخیص هوش مصنوعی")
        val local=quick.indexOf("تشخیص محلی ( آفلاین )")
        assertTrue(ai>=0&&local>ai)
        assertTrue(quick.substring(0,local).contains("Button("))
    }
}
