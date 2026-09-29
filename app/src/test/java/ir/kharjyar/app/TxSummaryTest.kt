package ir.kharjyar.app

import ir.kharjyar.app.core.balance.TxSummarizer
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxSource
import ir.kharjyar.app.data.db.TxStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TxSummaryTest {

    private fun tx(
        id: Long,
        accountId: Long,
        amount: Long,
        nature: Int,
        direction: Int,
        at: Long,
        status: Int = TxStatus.CONFIRMED
    ) = TransactionEntity(
        id = id,
        accountId = accountId,
        amountRial = amount,
        direction = direction,
        nature = nature,
        categoryId = null,
        description = "",
        occurredAt = at,
        recordedAt = at,
        source = TxSource.MANUAL,
        status = status
    )

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun `filters by account`() {
        val txs = listOf(
            tx(1, 10, 100_000, TxNature.INCOME, TxDirection.DEPOSIT, 5 * day),
            tx(2, 10, 30_000, TxNature.EXPENSE, TxDirection.WITHDRAW, 6 * day),
            tx(3, 20, 900_000, TxNature.INCOME, TxDirection.DEPOSIT, 6 * day)
        )
        val all = TxSummarizer.summarize(txs)
        assertEquals(1_000_000L, all.incomeRial)
        assertEquals(30_000L, all.expenseRial)

        val one = TxSummarizer.summarize(txs, accountId = 10)
        assertEquals(100_000L, one.incomeRial)
        assertEquals(30_000L, one.expenseRial)
        assertEquals(70_000L, one.netRial)

        val other = TxSummarizer.summarize(txs, accountId = 20)
        assertEquals(900_000L, other.incomeRial)
        assertEquals(0L, other.expenseRial)
    }

    @Test
    fun `filters by range but keeps everything when range is open`() {
        val txs = listOf(
            tx(1, 10, 100_000, TxNature.INCOME, TxDirection.DEPOSIT, 1 * day),
            tx(2, 10, 200_000, TxNature.INCOME, TxDirection.DEPOSIT, 10 * day)
        )
        // بازه‌ای که فقط تراکنش دوم را می‌گیرد (to غیرشامل است)
        val ranged = TxSummarizer.summarize(txs, from = 5 * day, to = 11 * day)
        assertEquals(200_000L, ranged.incomeRial)

        // بدون بازه: داده‌های قدیمی (مثلاً بازیابی‌شده از بکاپ) هم دیده می‌شوند
        val open = TxSummarizer.summarize(txs)
        assertEquals(300_000L, open.incomeRial)
    }

    @Test
    fun `transfers are neither income nor expense`() {
        val txs = listOf(
            tx(1, 10, 500_000, TxNature.TRANSFER, TxDirection.WITHDRAW, day),
            tx(2, 20, 500_000, TxNature.TRANSFER, TxDirection.DEPOSIT, day)
        )
        val s = TxSummarizer.summarize(txs)
        assertEquals(0L, s.incomeRial)
        assertEquals(0L, s.expenseRial)
        assertTrue(s.isEmpty)
    }

    @Test
    fun `pending transactions are counted separately`() {
        val txs = listOf(
            tx(1, 10, 100_000, TxNature.INCOME, TxDirection.DEPOSIT, day, TxStatus.PENDING),
            tx(2, 10, 40_000, TxNature.EXPENSE, TxDirection.WITHDRAW, day, TxStatus.PENDING),
            tx(3, 10, 60_000, TxNature.EXPENSE, TxDirection.WITHDRAW, day, TxStatus.CONFIRMED)
        )
        val s = TxSummarizer.summarize(txs, accountId = 10)
        assertEquals(0L, s.incomeRial)
        assertEquals(60_000L, s.expenseRial)
        assertEquals(2, s.pendingCount)
        assertEquals(100_000L, s.pendingIncomeRial)
        assertEquals(40_000L, s.pendingExpenseRial)
        assertFalse(s.isEmpty)
    }

    @Test
    fun `unknown nature falls back to bank direction`() {
        val txs = listOf(
            tx(1, 10, 100_000, TxNature.UNKNOWN, TxDirection.DEPOSIT, day),
            tx(2, 10, 25_000, TxNature.UNKNOWN, TxDirection.WITHDRAW, day)
        )
        val s = TxSummarizer.summarize(txs)
        assertEquals(100_000L, s.incomeRial)
        assertEquals(25_000L, s.expenseRial)
    }

    @Test
    fun `restored old transactions show up for their account outside the current month`() {
        // سناریوی باگ: بکاپ بازیابی می‌شود و تراکنش‌ها تاریخ قدیمی دارند.
        val monthFrom = 100 * day
        val monthTo = 130 * day
        val restored = listOf(
            tx(1, 7, 2_000_000, TxNature.INCOME, TxDirection.DEPOSIT, 40 * day),
            tx(2, 7, 500_000, TxNature.EXPENSE, TxDirection.WITHDRAW, 41 * day),
            tx(3, 8, 900_000, TxNature.EXPENSE, TxDirection.WITHDRAW, 42 * day)
        )
        val month = TxSummarizer.summarize(restored, accountId = 7, from = monthFrom, to = monthTo)
        assertTrue("در ماه جاری داده‌ای نیست", month.isEmpty)

        val all = TxSummarizer.summarize(restored, accountId = 7)
        assertEquals(2_000_000L, all.incomeRial)
        assertEquals(500_000L, all.expenseRial)
        assertEquals(1_500_000L, all.netRial)
    }
}
