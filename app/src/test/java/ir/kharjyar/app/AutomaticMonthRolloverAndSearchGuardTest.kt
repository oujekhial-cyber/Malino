package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AutomaticMonthRolloverAndSearchGuardTest {
 @Test fun `summary rolls at Tehran midnight and dashboard invalidates remembered range`() {
  val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText()
  val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
  assertTrue(vm.contains("private val calendarDay = flow"))
  assertTrue(vm.contains("atStartOfDay(PersianDate.TEHRAN)")&&vm.contains("combine(allTransactions, defaultAccount, _summaryRange, calendarDay)"))
  assertTrue(dashboard.contains("remember(summary.monthTitle) { viewModel.repo.currentPersianMonthRange() }"))
 }
 @Test fun `transaction search filters a chosen Persian month period`() {
  val s=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
  assertTrue(s.contains("TransactionMonthPeriod")&&s.contains("ماه"))
  assertTrue(s.contains("filterPeriod.contains(tx.occurredAt)"))
  assertTrue(s.contains("PersianDate.fromMillis(it.occurredAt)"))
  assertTrue(s.contains("همه ماه‌ها"))
 }
}
