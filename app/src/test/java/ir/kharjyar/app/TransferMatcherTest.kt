package ir.kharjyar.app

import ir.kharjyar.app.core.transfer.TransferCandidate
import ir.kharjyar.app.core.transfer.TransferMatch
import ir.kharjyar.app.core.transfer.TransferMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferMatcherTest {

    private val base = 1_700_000_000_000L

    @Test
    fun `two sides of internal transfer match`() {
        val withdraw = TransferCandidate(1, accountId = 10, amountRial = 5_000_000, direction = 1, occurredAt = base)
        val deposit = TransferCandidate(2, accountId = 20, amountRial = 5_000_000, direction = 0, occurredAt = base + 60_000)
        val m = TransferMatcher.match(deposit, listOf(withdraw))
        assertEquals(TransferMatch.Matched(1L), m)
    }

    @Test
    fun `same direction does not match`() {
        val w1 = TransferCandidate(1, 10, 5_000_000, 1, base)
        val w2 = TransferCandidate(2, 20, 5_000_000, 1, base + 1000)
        assertEquals(TransferMatch.None, TransferMatcher.match(w2, listOf(w1)))
    }

    @Test
    fun `same account does not match`() {
        val w = TransferCandidate(1, 10, 5_000_000, 1, base)
        val d = TransferCandidate(2, 10, 5_000_000, 0, base + 1000)
        assertEquals(TransferMatch.None, TransferMatcher.match(d, listOf(w)))
    }

    @Test
    fun `different amount does not match - fee is separate`() {
        // کارمزد باعث تفاوت مبلغ می‌شود؛ کارمزد باید تراکنش هزینه جداگانه باشد
        val w = TransferCandidate(1, 10, 5_007_200, 1, base)
        val d = TransferCandidate(2, 20, 5_000_000, 0, base + 1000)
        assertEquals(TransferMatch.None, TransferMatcher.match(d, listOf(w)))
    }

    @Test
    fun `outside window does not match`() {
        val w = TransferCandidate(1, 10, 5_000_000, 1, base)
        val d = TransferCandidate(2, 20, 5_000_000, 0, base + TransferMatcher.DEFAULT_WINDOW_MILLIS + 1)
        assertEquals(TransferMatch.None, TransferMatcher.match(d, listOf(w)))
    }

    @Test
    fun `multiple candidates are ambiguous need user confirmation`() {
        val w1 = TransferCandidate(1, 10, 5_000_000, 1, base)
        val w2 = TransferCandidate(2, 30, 5_000_000, 1, base + 5_000)
        val d = TransferCandidate(3, 20, 5_000_000, 0, base + 10_000)
        val m = TransferMatcher.match(d, listOf(w1, w2))
        assertTrue(m is TransferMatch.Ambiguous)
        assertEquals(2, (m as TransferMatch.Ambiguous).candidateTxIds.size)
    }

    @Test
    fun `equal amount purchases are not merged as transfer`() {
        // دو خرید هم‌مبلغ (هر دو برداشت) هرگز یک انتقال نمی‌شوند
        val p1 = TransferCandidate(1, 10, 300_000, 1, base)
        val p2 = TransferCandidate(2, 10, 300_000, 1, base + 20_000)
        assertEquals(TransferMatch.None, TransferMatcher.match(p2, listOf(p1)))
    }
}
