package ir.kharjyar.app

import ir.kharjyar.app.core.card.CardScan
import ir.kharjyar.app.core.card.CardScanParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** تحلیل متن OCR کارت بانکی؛ همه چیز محلی و بدون شبکه. */
class CardScanParserTest {

    private val year = 1405

    private fun parse(text: String) = CardScanParser.parse(text, currentJalaliYear = year)

    @Test
    fun `reads a typical front of card`() {
        val scan = parse(
            """
            بانک ملی ایران
            6037 9912 3456 7893
            05/09
            CVV2: 123
            رضا محمدی
            """.trimIndent()
        )

        assertEquals("6037991234567893", scan.cardNumber)
        assertEquals("09/1405", scan.expiry)
        assertEquals("123", scan.cvv2)
        assertEquals("ملی", scan.bankName)
    }

    @Test
    fun `reads persian digits`() {
        val scan = parse("۶۰۳۷ ۹۹۱۲ ۳۴۵۶ ۷۸۹۳\nانقضا ۰۶/۱۴۰۸\ncvv2 ۴۵۶۷")

        assertEquals("6037991234567893", scan.cardNumber)
        assertEquals("06/1408", scan.expiry)
        assertEquals("4567", scan.cvv2)
    }

    @Test
    fun `wrong card number is rejected instead of guessed`() {
        // یک رقم تغییر کرده؛ Luhn رد می‌شود
        val scan = parse("6037 9912 3456 7894")

        assertTrue("عدد نامعتبر نباید در فرم بنشیند", scan.cardNumber.isBlank())
        assertTrue(scan.isEmpty)
    }

    @Test
    fun `cvv2 without a label is ignored`() {
        val scan = parse("6037 9912 3456 7893\n123")

        assertEquals("6037991234567893", scan.cardNumber)
        assertTrue("عدد بی‌برچسب نباید CVV2 فرض شود", scan.cvv2.isBlank())
    }

    @Test
    fun `cvv2 stuck to the card number does not break it`() {
        val scan = parse("6037 9912 3456 7893 456\nCVV2 456")

        assertEquals("6037991234567893", scan.cardNumber)
        assertEquals("456", scan.cvv2)
    }

    @Test
    fun `reads iban with IR prefix and spaces`() {
        val scan = parse("شماره شبا\nIR27 0170 0000 0010 0324 2000 01")

        assertEquals("270170000000100324200001", scan.iban)
    }

    @Test
    fun `iban with broken checksum and no marker is dropped`() {
        val scan = parse("990170000000100324200001")

        assertTrue(scan.iban.isBlank())
    }

    @Test
    fun `iban does not leak into the card number`() {
        val scan = parse("IR27 0170 0000 0010 0324 2000 01")

        assertEquals("270170000000100324200001", scan.iban)
        assertTrue("۱۶ رقم اول شبا نباید شماره کارت شود", scan.cardNumber.isBlank())
    }

    @Test
    fun `four digit year form is month slash year`() {
        assertEquals("06/1408", parse("06/1408").expiry)
    }

    @Test
    fun `two digit form follows the iranian year slash month order`() {
        // ۰۵/۰۹ روی کارت یعنی آذر ۱۴۰۵
        assertEquals("09/1405", parse("05/09").expiry)
    }

    @Test
    fun `a value above twelve can only be the year`() {
        assertEquals("11/1414", parse("11/14").expiry)
        assertEquals("11/1414", parse("14/11").expiry)
        assertEquals("07/1409", parse("07-1409").expiry)
    }

    @Test
    fun `unlabeled four digits are not read as expiry`() {
        assertTrue(parse("1234").expiry.isBlank())
    }

    @Test
    fun `labeled four digits are read as year then month`() {
        assertEquals("08/1407", parse("انقضا ۰۷۰۸").expiry)
    }

    @Test
    fun `expiry far in the past is not accepted`() {
        assertTrue(parse("80/09").expiry.isBlank())
    }

    @Test
    fun `account number is read only when labeled`() {
        val scan = parse("شماره حساب: 1234.56.7890123.1")

        assertEquals("1234.56.7890123.1", scan.accountNumber)
    }

    @Test
    fun `account number digits are not mistaken for a card`() {
        val scan = parse("شماره حساب 1234.56.7890123.1")

        assertTrue(scan.cardNumber.isBlank())
    }

    @Test
    fun `bank is guessed from the bin`() {
        assertEquals("توسعه تعاون", CardScanParser.bankOfCard("5029081234123418"))
        assertEquals("ملت", CardScanParser.bankOfCard("6104331111222239"))
        assertEquals("", CardScanParser.bankOfCard("1234"))
    }

    @Test
    fun `frames accumulate without overwriting earlier finds`() {
        val first = CardScan(cardNumber = "6037991234567893")
        val second = CardScan(cardNumber = "9999999999999999", cvv2 = "123")

        val merged = first.mergedWith(second)

        assertEquals("6037991234567893", merged.cardNumber)
        assertEquals("123", merged.cvv2)
    }

    @Test
    fun `card basics decide when scanning can stop`() {
        val partial = CardScan(cardNumber = "6037991234567893", expiry = "09/1405")
        assertFalse(partial.hasCardBasics)
        assertTrue(partial.copy(cvv2 = "123").hasCardBasics)
    }

    @Test
    fun `luhn and iban checks work`() {
        assertTrue(CardScanParser.isLuhnValid("6037991234567893"))
        assertFalse(CardScanParser.isLuhnValid("6037991234567894"))
        assertFalse(CardScanParser.isLuhnValid("603799123456789"))
        assertTrue(CardScanParser.isIbanValid("270170000000100324200001"))
        assertFalse(CardScanParser.isIbanValid("280170000000100324200001"))
    }

    @Test
    fun `empty or noisy text yields nothing`() {
        assertTrue(parse("").isEmpty)
        assertTrue(parse("بانک نمونه — کارت هدیه").isEmpty)
    }

    @Test
    fun `reads a vertical card printed in four rows`() {
        val scan = parse(
            """
            بانک ملی ایران
            6037
            9912
            3456
            7893
            05/09
            123
            """.trimIndent()
        )

        assertEquals("6037991234567893", scan.cardNumber)
        assertEquals("09/1405", scan.expiry)
        assertEquals("123", scan.cvv2)
        assertEquals("ملی", scan.bankName)
    }

    @Test
    fun `two eight digit blocks also make a card number`() {
        val scan = parse(
            """
            60379912
            34567893
            """.trimIndent()
        )
        assertEquals("6037991234567893", scan.cardNumber)
    }

    @Test
    fun `unrelated numbers do not become a card number`() {
        val scan = parse(
            """
            شعبه 1234
            تلفن 5678
            کد 9012
            صندوق 3456
            """.trimIndent()
        )
        assertEquals("", scan.cardNumber)
    }

    @Test
    fun `new bins are recognized`() {
        assertEquals("توسعه صادرات", CardScanParser.bankOfCard("6276481234567890"))
        assertEquals("سرمایه", CardScanParser.bankOfCard("6396071234567890"))
        assertEquals("مؤسسه اعتباری ملل", CardScanParser.bankOfCard("6062561234567890"))
    }
}
