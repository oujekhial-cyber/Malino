package ir.kharjyar.app

import ir.kharjyar.app.core.sms.SmsClassifier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SmsHistoryFinancialFilteringGuardTest {
    @Test fun `otp variants never enter historical financial import`() {
        assertTrue(SmsClassifier.isOtp("رمز شما: ۴۸۲۹۱۷؛ تا ۲ دقیقه معتبر است"))
        assertTrue(SmsClassifier.isOtp("رمز جهت خرید اینترنتی 731905 مدت اعتبار 120 ثانیه"))
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
        assertTrue(source.contains("if(SmsClassifier.isOtp(body)||SmsClassifier.classify(body)==SmsKind.NON_FINANCIAL)continue"))
    }

    @Test fun `transactions without account digits use a unique saved sender mapping`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
        assertTrue(source.contains("val senderAccountIds=mappings.filter"))
        assertTrue(source.contains("val aid=numberAccountId?:senderAccountIds.singleOrNull()"))
        assertTrue(source.contains("SmsClassifier.classify(body)==SmsKind.NON_FINANCIAL"))
        assertFalse(source.contains("SmsClassifier.classify(body)!=SmsKind.FINANCIAL_LIKELY"))
    }
}
