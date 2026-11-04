package ir.kharjyar.app

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.date.persianYearChoices
import ir.kharjyar.app.ui.screens.TransactionMonthPeriod
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class PersianYearMonthFilterTest {
    @Test fun `year choices run from 1400 through 1450 inclusive`() {
        val choices = persianYearChoices()
        assertEquals((1400..1450).toList(), choices)
        assertEquals(51, choices.size)
    }

    @Test fun `year and month controls share a row with equal weights`() {
        val transaction = File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
        val report = File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
        listOf(transaction.substringAfter("// سال و ماه مستقل‌اند").substringBefore("label=\"حساب\""),
            report.substringAfter("// ---------- فیلترها").substringBefore("label = \"حساب\"")).forEach { section ->
            assertTrue(section.contains("Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp))"))
            assertEquals(2, Regex("Modifier\\.weight\\(1f\\)").findAll(section).count())
            assertTrue(section.indexOf("سال") < section.indexOf("ماه"))
        }
    }

    @Test fun `transaction year and month filter independently including all options`() {
        val farvardin = PersianDate(1406, 1, 1).startOfDayMillis()
        val nextMonth = PersianDate(1406, 2, 1).startOfDayMillis()
        val previousYear = PersianDate(1405, 1, 1).startOfDayMillis()
        assertTrue(TransactionMonthPeriod(1406, 1, "").contains(farvardin))
        assertFalse(TransactionMonthPeriod(1406, 1, "").contains(nextMonth))
        assertFalse(TransactionMonthPeriod(1406, 1, "").contains(previousYear))
        assertTrue(TransactionMonthPeriod(1406, null, "").contains(nextMonth))
        assertTrue(TransactionMonthPeriod(null, 1, "").contains(previousYear))
        assertFalse(TransactionMonthPeriod(null, 1, "").contains(nextMonth))
        assertTrue(TransactionMonthPeriod(null, null, "").contains(nextMonth))
    }

    @Test fun `report year selection applies to monthly and annual ranges`() {
        val report = File("src/main/java/ir/kharjyar/app/ui/screens/ReportsScreen.kt").readText()
        assertTrue(report.contains("label = \"سال گزارش\""))
        assertTrue(report.contains("label = \"ماه گزارش\""))
        assertTrue(report.contains("PersianDate(year, session.month.month, 1)"))
        assertTrue(report.contains("PersianDate(session.month.year, 1, 1).startOfDayMillis()"))
        assertTrue(report.contains("options = (1..12).toList()"))
    }
}
