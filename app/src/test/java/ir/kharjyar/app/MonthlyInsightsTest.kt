package ir.kharjyar.app

import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.report.monthSummary
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.data.prefs.parseCategoryBudgets
import ir.kharjyar.app.data.prefs.encodeCategoryBudgets
import ir.kharjyar.app.ui.screens.potentialDuplicatePairs
import org.junit.Assert.*
import org.junit.Test

class MonthlyInsightsTest {
    private fun tx(id: Long, at: Long, nature: Int, status: Int = TxStatus.CONFIRMED, deleted: Long? = null) = TransactionEntity(
        id = id, accountId = 1, amountRial = 100, direction = TxDirection.WITHDRAW, nature = nature,
        categoryId = 7, occurredAt = at, recordedAt = at, status = status, deletedAt = deleted)

    @Test fun `month filters confirmed active expenses and ignores transfers`() {
        val month = PersianDate(1403, 1, 1)
        val start = month.startOfDayMillis()
        val end = month.lastOfMonth().endOfDayMillisExclusive()
        val rows = listOf(tx(1,start,TxNature.EXPENSE), tx(2,end,TxNature.EXPENSE), tx(3,start,TxNature.EXPENSE,TxStatus.PENDING),
            tx(4,start,TxNature.EXPENSE,deleted=1), tx(5,start,TxNature.TRANSFER), tx(6,start,TxNature.INCOME))
        val result = monthSummary(rows, month)
        assertEquals(100L, result.expense)
        assertEquals(100L, result.income)
        assertEquals(100L, result.categories[7L])
        assertEquals(PersianDate(1402,12,1), month.plusMonths(-1))
    }
    @Test fun `budget serialization handles malformed input and preserves limits`() {
        assertEquals(mapOf(1L to 50L, 8L to 100L), parseCategoryBudgets("1:50,broken,8:100,-1:30,2:-3"))
        assertEquals("1:50,8:100", encodeCategoryBudgets(mapOf(8L to 100L,1L to 50L)))
    }
    @Test fun `correction preserves later balance changes and refuses overflow`() {
        assertEquals(950L, AccountBalance.correctedAnchorOffset(900L, 1000L, 1050L))
        assertNull(AccountBalance.correctedAnchorOffset(Long.MAX_VALUE, 0L, 1L))
    }
    @Test fun `duplicates only suggest nearby active confirmed same account and amount`() {
        val rows = listOf(tx(1,1_000,TxNature.EXPENSE),tx(2,2_000,TxNature.EXPENSE),tx(3,3_000,TxNature.EXPENSE,TxStatus.PENDING),tx(4,500_000,TxNature.EXPENSE))
        assertEquals(listOf(1L to 2L), potentialDuplicatePairs(rows).map { it.first.id to it.second.id })
    }
}
