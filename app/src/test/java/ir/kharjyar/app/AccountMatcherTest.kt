package ir.kharjyar.app

import ir.kharjyar.app.core.sms.AccountMatch
import ir.kharjyar.app.core.sms.AccountMatcher
import ir.kharjyar.app.core.sms.SenderMapping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountMatcherTest {

    @Test
    fun `unknown sender returns unknown`() {
        val m = AccountMatcher.match("99999", "برداشت 1000", emptyList())
        assertEquals(AccountMatch.Unknown, m)
    }

    @Test
    fun `single generic mapping matches`() {
        val mappings = listOf(SenderMapping(1, 10, "700701", ""))
        val m = AccountMatcher.match("700701", "برداشت: 1,000 مانده: 5,000", mappings)
        assertEquals(AccountMatch.Single(10L), m)
    }

    @Test
    fun `two accounts same sender disambiguated by identifier`() {
        val mappings = listOf(
            SenderMapping(1, 10, "700701", "1234"),
            SenderMapping(2, 20, "700701", "5678")
        )
        val m = AccountMatcher.match("700701", "برداشت از حساب *5678 مبلغ: 1,000", mappings)
        assertEquals(AccountMatch.Single(20L), m)
    }

    @Test
    fun `two accounts same sender no identifier in body is ambiguous`() {
        val mappings = listOf(
            SenderMapping(1, 10, "700701", "1234"),
            SenderMapping(2, 20, "700701", "5678")
        )
        val m = AccountMatcher.match("700701", "برداشت مبلغ: 1,000 بدون شناسه", mappings)
        assertTrue(m is AccountMatch.Ambiguous)
        assertEquals(setOf(10L, 20L), (m as AccountMatch.Ambiguous).accountIds.toSet())
    }

    @Test
    fun `one bank multiple senders`() {
        val mappings = listOf(
            SenderMapping(1, 10, "700701", ""),
            SenderMapping(2, 10, "700702", "")
        )
        assertEquals(AccountMatch.Single(10L), AccountMatcher.match("700701", "برداشت", mappings))
        assertEquals(AccountMatch.Single(10L), AccountMatcher.match("700702", "برداشت", mappings))
    }

    @Test
    fun `sender normalization with country code`() {
        val mappings = listOf(SenderMapping(1, 10, "+98700701", ""))
        val m = AccountMatcher.match("700701", "برداشت", mappings)
        assertEquals(AccountMatch.Single(10L), m)
    }

    @Test
    fun `identifier matched inside longer card number`() {
        val mappings = listOf(SenderMapping(1, 10, "700701", "1234"))
        val m = AccountMatcher.match("700701", "خرید با کارت 6037-99**-****-1234 مبلغ 1000", mappings)
        assertEquals(AccountMatch.Single(10L), m)
    }

    @Test
    fun `persian digits identifier in body`() {
        val mappings = listOf(SenderMapping(1, 10, "700701", "1234"))
        val m = AccountMatcher.match("700701", "برداشت از حساب ٭۱۲۳۴", mappings)
        assertEquals(AccountMatch.Single(10L), m)
    }
}
