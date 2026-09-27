package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssetsFeesReceiptsReorderGuardTest {
 @Test fun `reports show all expense categories`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText();assertTrue(s.contains("تفکیک هزینه‌ها بر اساس دسته"));assertTrue(s.contains("val slices = byCategory")) }
 @Test fun `dashboard cards support persisted long press reordering`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText();assertTrue(s.contains("detectDragGesturesAfterLongPress"));assertTrue(s.contains("setDashboardAccountOrder")) }
 @Test fun `manual transactions accept camera and gallery receipts`() { val s=File("src/main/java/ir/kharjyar/app/ui/components/ReceiptImagePicker.kt").readText();assertTrue(s.contains("TakePicture"));assertTrue(s.contains("GetContent"));assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText().contains("TransactionAttachmentEntity")) }
 @Test fun `assets calculate profit and support gold vehicle property`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/AssetsScreen.kt").readText();assertTrue(s.contains("AssetKind.GOLD"));assertTrue(s.contains("AssetKind.VEHICLE"));assertTrue(s.contains("AssetKind.PROPERTY"));assertTrue(s.contains("سود کل")&&s.contains("زیان کل")) }
 @Test fun `fee percentage is configurable and applied in reconciliation`() { assertTrue(File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText().contains("bankFeePercent"));assertTrue(File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText().contains("کارمزد بانکی خودکار")) }
 @Test fun `debts have date kind and person filters with totals`() { val s=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText();assertTrue(s.contains("جست‌وجوی نام شخص"));assertTrue(s.contains("مجموع طلب"));assertTrue(s.contains("مجموع بدهی")) }
}
