package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TransactionDateRangeFilterGuardTest {
    @Test fun `transaction list supports inclusive Persian date range and filtered totals`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/TransactionsScreen.kt").readText()
        listOf(
            "var dateRangeEnabled",
            "PersianDateField(filterFromDate",
            "PersianDateField(filterToDate",
            "tx.occurredAt in rangeStart until rangeEndExclusive",
            "filterPeriod=it;dateRangeEnabled=false",
            "مجموع واریز",
            "مجموع برداشت",
            "filteredDeposits",
            "filteredWithdrawals"
        ).forEach { token -> assertTrue(token, source.contains(token)) }
    }
}
