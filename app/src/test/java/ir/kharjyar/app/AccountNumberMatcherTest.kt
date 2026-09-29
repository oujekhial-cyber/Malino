package ir.kharjyar.app

import ir.kharjyar.app.core.sms.AccountMatch
import ir.kharjyar.app.core.sms.AccountNumberMatcher
import ir.kharjyar.app.core.sms.MatchableAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountNumberMatcherTest {
    private val accounts = listOf(
        MatchableAccount(1, "****1234", "240430659182671", "820540102680020817909002", "6037997512341234"),
        MatchableAccount(2, "****9876", "010123456789", "", "6104337890129876")
    )

    @Test fun `matches masked card suffix in bank sms`() {
        assertEquals(AccountMatch.Single(1), AccountNumberMatcher.match("برداشت از کارت 6037****1234 مبلغ 500000 ریال", accounts))
    }

    @Test fun `matches dotted account number`() {
        assertEquals(AccountMatch.Single(1), AccountNumberMatcher.match("برداشت از 2404.306.5918267.1", accounts))
    }

    @Test fun `matches four five or six digits from anywhere in account`() {
        assertEquals(AccountMatch.Single(1), AccountNumberMatcher.match("حساب مرتبط 306.59", accounts))
        assertEquals(AccountMatch.Single(1), AccountNumberMatcher.match("شناسه حساب 591826", accounts))
        assertEquals(AccountMatch.Single(2), AccountNumberMatcher.match("حساب 12345", accounts))
    }

    @Test fun `middle match works across separators rather than only suffix`() {
        assertEquals(AccountMatch.Single(1), AccountNumberMatcher.match("برداشت از حساب 430.659", accounts))
    }

    @Test fun `stronger six digit match wins over weaker four digit candidate`() {
        val overlapping = accounts + MatchableAccount(3, "", "999943009999", "", "")
        assertEquals(AccountMatch.Single(1), AccountNumberMatcher.match("حساب 430659", overlapping))
    }

    @Test fun `does not mistake unrelated amount for an account`() {
        assertTrue(AccountNumberMatcher.match("برداشت مبلغ 500000 ریال", accounts) is AccountMatch.Unknown)
    }
}
