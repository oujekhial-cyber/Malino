package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class PdfReportIdentityGuardTest {
 @Test fun `pdf states exact report start and end dates`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
  assertTrue(source.contains("بازه گزارش: \$reportRangeText"))
  assertTrue(source.contains("از تاریخ \${PersianDate.fromMillis(from).format()} تا تاریخ"))
  assertTrue(source.contains("PersianDate.formatDateTime(from)"))
  assertTrue(source.contains("PersianDate.formatDateTime(to - 60_000L)"))
 }
 @Test fun `pdf uses full stored account number rather than four digit mask`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
  assertTrue(source.contains("account.accountNumber.isNotBlank() -> account.accountNumber.trim()"))
  assertTrue(source.contains("pdfAccountLabel"))
  assertFalse(source.contains("\"****\" + digits.takeLast(4)"))
  val pdf=File("src/main/java/ir/kharjyar/app/pdf/PdfExporter.kt").readText()
  assertTrue(pdf.contains("floatArrayOf(88f,120f,67f,52f,110f,94f)"))
 }
}
