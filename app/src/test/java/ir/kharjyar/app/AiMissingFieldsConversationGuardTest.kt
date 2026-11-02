package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiMissingFieldsConversationGuardTest {
    private val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()

    @Test fun `command center asks for every required transaction slot`() {
        assertTrue(quick.contains("مبلغ تراکنش چقدر بود؟"))
        assertTrue(quick.contains("این مبلغ واریز بوده یا برداشت؟"))
        assertTrue(quick.contains("از کدام حساب پرداخت کردید؟"))
        assertTrue(quick.contains("مبلغ به کدام حساب واریز شده؟"))
        assertTrue(quick.contains("این تراکنش مربوط به کدام دسته‌بندی است؟"))
        assertTrue(quick.contains("تراکنش در چه تاریخی انجام شد؟"))
    }

    @Test fun `confirmation stays disabled until required slots are resolved`() {
        assertTrue(quick.contains("readyToConfirm = p.isComplete && p.dateExplicit"))
        assertTrue(quick.contains("p.categoryId != null"))
        assertTrue(quick.contains("enabled = readyToConfirm"))
        assertTrue(quick.contains("parsed=p.copy"))
    }
}
