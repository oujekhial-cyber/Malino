package ir.kharjyar.app

import ir.kharjyar.app.core.date.PersianDate
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ReportMonthAndCategoryDrilldownGuardTest {
    private val report = File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()

    @Test fun `selected month uses exclusive Jalali month boundaries including leap Esfand`() {
        val leap = PersianDate(1399, 12, 1)
        val next = PersianDate(1400, 1, 1)
        assertEquals(30, leap.monthLength())
        assertEquals(next.startOfDayMillis(), leap.lastOfMonth().endOfDayMillisExclusive())
        assertEquals(29, PersianDate(1400, 12, 1).monthLength())
        assertTrue(report.contains("session.month.startOfDayMillis() to session.month.lastOfMonth().endOfDayMillisExclusive()"))
        assertTrue(report.contains("tx.occurredAt >= from && tx.occurredAt < to"))
    }

    @Test fun `month filter drives summaries and both exports`() {
        assertTrue(report.contains("label = \"ماه گزارش\""))
        assertTrue(report.contains("range = RangeKind.MONTH"))
        assertTrue(report.contains("val confirmed = filtered.filter"))
        assertTrue(report.contains("buildDailySeries(filtered, days, PersianDate.fromMillis"))
        assertEquals(2, Regex("filtered.sortedByDescending").findAll(report).count())
    }

    @Test fun `category drilldown uses ids including uncategorized and shows all matching rows`() {
        assertTrue(report.contains("session.drilldownCategory = slice.id"))
        assertTrue(report.contains("it.categoryId == session.drilldownCategory"))
        assertTrue(report.contains("val count=detailTransactions.size"))
        assertTrue(report.contains("detailTotal/count"))
        assertTrue(report.contains("if (session.drilldownActive) detailTransactions else detailTransactions.take(20)"))
    }

    @Test fun `filters live only in view model session`() {
        val vm = File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText()
        assertTrue(vm.contains("val reportFilterSession ="))
        assertTrue(report.contains("var month by mutableStateOf(today.firstOfMonth())"))
        assertTrue(report.contains("val session = viewModel.reportFilterSession"))
        assertFalse(report.contains("rememberSaveable"))
    }
}
