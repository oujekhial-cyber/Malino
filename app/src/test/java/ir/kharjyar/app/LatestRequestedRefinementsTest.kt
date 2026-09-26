package ir.kharjyar.app

import ir.kharjyar.app.core.sms.SmsClassifier
import ir.kharjyar.app.core.sms.SmsKind
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LatestRequestedRefinementsTest {
 @Test fun `security bank notices are excluded from financial history`() {
  assertEquals(SmsKind.NON_FINANCIAL,SmsClassifier.classify("ورود به همراه بانک شما در ساعت 12:30 انجام شد"))
  assertEquals(SmsKind.NON_FINANCIAL,SmsClassifier.classify("رمز ورود اشتباه بود کد 123456"))
  assertEquals(SmsKind.NON_FINANCIAL,SmsClassifier.classify("رمز پویا 987654 فقط تا دو دقیقه معتبر است"))
 }
 @Test fun `transaction deletion asks for confirmation`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText();assertTrue(s.contains("آیا این تراکنش حذف شود؟"));assertTrue(s.contains("pendingDelete")) }
 @Test fun `bank balance snapshots remain separate from estimated balance`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();assertTrue(s.contains("مانده آخرین پیامک بانک"));assertTrue(s.contains("یافتن تراکنش ثبت‌نشده")) }
 @Test fun `widget elements move directly on preview`() { val s=File("src/main/java/ir/kharjyar/app/ui/components/WidgetPreview.kt").readText();assertTrue(s.contains("detectDragGestures"));assertTrue(s.contains("DraggablePreviewElement")) }
 @Test fun `check supports camera gallery and OCR review`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText();assertTrue(s.contains("TakePicture"));assertTrue(s.contains("GetContent"));assertTrue(s.contains("لطفاً اطلاعات تکمیل‌شده را بررسی کنید")) }
}
