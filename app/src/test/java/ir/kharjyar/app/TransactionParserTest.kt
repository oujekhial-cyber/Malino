package ir.kharjyar.app

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.nlp.ParserAccount
import ir.kharjyar.app.core.nlp.ParserCategory
import ir.kharjyar.app.core.nlp.PersianNumbers
import ir.kharjyar.app.core.nlp.TransactionParser
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** تحلیل جمله فارسی برای ثبت سریع تراکنش. */
class TransactionParserTest {

    private val accounts = listOf(
        ParserAccount(1L, "روزمره", "توسعه تعاون"),
        ParserAccount(2L, "پس‌انداز", "ملت")
    )
    private val categories = listOf(
        ParserCategory(10L, "خوراک و سوپرمارکت"),
        ParserCategory(11L, "حمل‌ونقل"),
        ParserCategory(12L, "حقوق")
    )
    private val today = PersianDate(1405, 7, 2)

    private fun parse(text: String, unit: MoneyUnit = MoneyUnit.RIAL) =
        TransactionParser.parse(text, accounts, categories, unit, today)

    // ---------- اعداد ----------

    @Test
    fun `parses digit and word numbers`() {
        assertEquals(250_000L, PersianNumbers.parseFirst("250 هزار"))
        assertEquals(250_000L, PersianNumbers.parseFirst("دویست و پنجاه هزار"))
        assertEquals(2_500_000L, PersianNumbers.parseFirst("دو میلیون و نیم"))
        assertEquals(500_000L, PersianNumbers.parseFirst("نیم میلیون"))
        assertEquals(320L, PersianNumbers.parseFirst("سیصد و بیست"))
        assertEquals(1_000_000_000L, PersianNumbers.parseFirst("یک میلیارد"))
    }

    @Test
    fun `parses mixed digits and words`() {
        assertEquals(2_500_000L, PersianNumbers.parseFirst("2 میلیون و 500 هزار"))
    }

    @Test
    fun `ignores thousands separators and persian digits`() {
        assertEquals(1_200_000L, PersianNumbers.parseFirst("۱,۲۰۰,۰۰۰"))
    }

    @Test
    fun `returns null when no number present`() {
        assertNull(PersianNumbers.parseFirst("رفتم خرید کردم"))
    }

    // ---------- جمله کامل ----------

    @Test
    fun `parses the canonical example`() {
        val r = parse("۲۵۰ هزار تومن کیک از سوپرمارکت خریدم با حساب روزمره")
        // «تومن» یعنی مبلغ باید به ریال ضرب در ده شود
        assertEquals(2_500_000L, r.amountRial)
        assertEquals(TxDirection.WITHDRAW, r.direction)
        assertEquals(TxNature.EXPENSE, r.nature)
        assertEquals(1L, r.accountId)
        assertEquals(10L, r.categoryId)
        assertTrue(r.isComplete)
    }

    @Test
    fun `description keeps only the meaningful subject`() {
        val r = parse("امروز ۲۵۰ هزار تومن کیک از سوپرمارکت با حساب روزمره خریدم")
        assertEquals("کیک از سوپرمارکت", r.description)
    }

    @Test
    fun `detects deposit from verbs`() {
        val r = parse("حقوق این ماه ۲۵ میلیون تومن واریز شد به پس‌انداز")
        assertEquals(TxDirection.DEPOSIT, r.direction)
        assertEquals(TxNature.INCOME, r.nature)
        assertEquals(2L, r.accountId)
        assertEquals(12L, r.categoryId)
    }

    @Test
    fun `honors unit words over default`() {
        // واحد پیش‌فرض ریال است ولی کاربر «تومان» گفته
        assertEquals(1_000_000L, parse("۱۰۰ هزار تومان از روزمره", MoneyUnit.RIAL).amountRial)
        // و برعکس: «ریال» صریح، حتی وقتی پیش‌فرض تومان است
        assertEquals(100_000L, parse("۱۰۰ هزار ریال از روزمره", MoneyUnit.TOMAN).amountRial)
    }

    @Test
    fun `falls back to default unit when unspecified`() {
        assertEquals(1_000_000L, parse("۱۰۰ هزار از روزمره", MoneyUnit.TOMAN).amountRial)
    }

    // ---------- تاریخ ----------

    @Test
    fun `understands relative dates`() {
        assertEquals(today.plusDays(-1), parse("دیروز ۵۰ هزار از روزمره").date)
        assertEquals(today.plusDays(-2), parse("پریروز ۵۰ هزار از روزمره").date)
        assertEquals(today, parse("امروز ۵۰ هزار از روزمره").date)
    }

    @Test
    fun `defaults to today and marks it implicit`() {
        val r = parse("۵۰ هزار از روزمره")
        assertEquals(today, r.date)
        assertFalse("تاریخ صریح گفته نشده", r.dateExplicit)
    }

    @Test
    fun `explicit date is flagged`() {
        assertTrue(parse("دیروز ۵۰ هزار از روزمره").dateExplicit)
    }

    // ---------- حساب ----------

    @Test
    fun `matches account by bank name when unique`() {
        val r = parse("۵۰ هزار از ملت برداشت کردم")
        assertEquals(2L, r.accountId)
    }

    @Test
    fun `warns when account cannot be determined`() {
        val r = parse("۵۰ هزار خرید کردم")
        assertNull(r.accountId)
        assertFalse("بدون حساب نباید کامل باشد", r.isComplete)
        assertTrue(r.warnings.any { it.contains("حساب") })
    }

    @Test
    fun `warns when amount is missing`() {
        val r = parse("از روزمره خرید کردم")
        assertNull(r.amountRial)
        assertFalse(r.isComplete)
        assertTrue(r.warnings.any { it.contains("مبلغ") })
    }

    // ---------- ماهیت ----------

    @Test
    fun `detects transfer`() {
        val r = parse("۵۰۰ هزار از روزمره کارت به کارت کردم")
        assertEquals(TxNature.TRANSFER, r.nature)
    }

    @Test
    fun `assumes withdrawal when verb is absent`() {
        val r = parse("۵۰ هزار روزمره")
        assertEquals(TxDirection.WITHDRAW, r.direction)
        assertTrue(r.warnings.any { it.contains("نوع") })
    }

    // ---------- اطمینان ----------

    @Test
    fun `confidence is high only for a clean parse`() {
        val clean = parse("۲۵۰ هزار تومن کیک از سوپرمارکت خریدم با حساب روزمره")
        assertEquals(ir.kharjyar.app.core.nlp.ParseConfidence.HIGH, clean.confidence)

        val messy = parse("یه چیزی خریدم")
        assertEquals(ir.kharjyar.app.core.nlp.ParseConfidence.LOW, messy.confidence)
    }

    @Test
    fun `never auto completes without amount and account`() {
        // مهم: نتیجه ناقص هرگز نباید قابل ثبت باشد
        listOf("خرید کردم", "۵۰ هزار", "دیروز").forEach {
            assertFalse("«$it» نباید کامل باشد", parse(it).isComplete)
        }
    }
}
