package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class ExcelReportSummaryGuardTest {
 @Test fun `excel report receives app name exact range totals and account identifier`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
  listOf("title = \"خرج‌یار — گزارش مالی\"","rangeText = \"بازه گزارش: \$reportRangeText\"","incomeText = Money.format(income","expenseText = Money.format(expense","netText = Money.format(income - expense","accountNumber = account?.let(::reportAccountNumber)").forEach { assertTrue(it,screen.contains(it)) }
  val exporter=File("src/main/java/ir/kharjyar/app/report/ExcelExporter.kt").readText()
  listOf("جمع واریز","جمع برداشت","خالص","شماره حساب","ردیف","automaticRowNumber","mergeCell ref=\\\"A1:H1","گزارش خرج‌یار").forEach { assertTrue(it,exporter.contains(it)) }
 }
}
