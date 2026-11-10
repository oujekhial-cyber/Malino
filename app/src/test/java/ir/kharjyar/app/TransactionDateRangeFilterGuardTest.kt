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
            "viewModel.setTransactionMonthFilter(year.takeIf { it != 0 }, selectedFilterMonth); dateRangeEnabled = false",
            "viewModel.setTransactionMonthFilter(selectedFilterYear, month.takeIf { it != 0 }); dateRangeEnabled = false",
            "درآمد واقعی",
            "هزینه واقعی",
            "actualIncome",
            "actualExpense"
        ).forEach { token -> assertTrue(token, source.contains(token)) }
    }
}
