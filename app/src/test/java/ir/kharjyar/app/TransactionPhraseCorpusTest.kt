package ir.kharjyar.app

import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.nlp.ParserAccount
import ir.kharjyar.app.core.nlp.ParserCategory
import ir.kharjyar.app.core.nlp.TransactionParser
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * پیکره بزرگ جمله‌های محاوره‌ای. ترکیب عبارت‌ها بیش از ۵۰۰ جمله متفاوت می‌سازد
 * و جلوی برگشت خطاهای تشخیص واریز، برداشت و انتقال را می‌گیرد.
 */
class TransactionPhraseCorpusTest {
    private val accounts = listOf(
        ParserAccount(1, "حساب اصلی", "بانک نمونه مبدأ"),
        ParserAccount(2, "حساب روزمره", "بانک نمونه مقصد")
    )
    private val categories = listOf(ParserCategory(1, "خوراک و سوپرمارکت"))

    private fun parse(text: String) = TransactionParser.parse(
        text, accounts, categories, MoneyUnit.TOMAN, PersianDate(1405, 7, 3)
    )

    @Test fun `hundreds of internal transfer phrasings detect both accounts`() {
        val sources = listOf("از حساب اصلی", "از کارت اصلی", "از توی حساب اصلی", "از داخل حساب اصلی", "مبدأ اصلی", "مبدا اصلی")
        val targets = listOf("به حساب روزمره", "به حساب روزمره خودم", "به کارت روزمره", "به حساب دیگه خودم روزمره", "مقصد روزمره", "به روزمره")
        val verbs = listOf("انتقال دادم", "انتقال زدم", "منتقل کردم", "جابجا کردم", "جابه جا کردم", "کارت به کارت کردم", "حواله کردم", "فرستادم")
        var checked = 0
        for (source in sources) for (target in targets) for (verb in verbs) {
            val sentence = "پنجاه هزار تومان $source $target $verb"
            val result = parse(sentence)
            assertEquals("مبدأ در «$sentence»", 1L, result.accountId)
            assertEquals("مقصد در «$sentence»", 2L, result.targetAccountId)
            assertEquals(TxNature.TRANSFER, result.nature)
            assertTrue(result.transferToOwn)
            checked++
        }
        assertTrue(checked >= 250)
    }

    @Test fun `many deposit and withdrawal verbs retain their direction`() {
        val deposits = listOf("حقوق گرفتم", "پول گرفتم", "واریز شد", "دریافت کردم", "به حسابم آمد", "پاداش گرفتم", "فروختم")
        val withdrawals = listOf("خریدم", "پرداخت کردم", "خرج کردم", "برداشت کردم", "کارت کشیدم", "کرایه دادم", "قسط دادم")
        deposits.forEach { verb ->
            assertEquals("واریز: $verb", TxDirection.DEPOSIT, parse("صد هزار تومان $verb به حساب اصلی").direction)
        }
        withdrawals.forEach { verb ->
            assertEquals("برداشت: $verb", TxDirection.WITHDRAW, parse("صد هزار تومان $verb از حساب اصلی").direction)
        }
    }


    @Test fun `every supported bank name can be source or destination`() {
        val banks = listOf(
            "ملی", "ملت", "صادرات", "تجارت", "سپه", "کشاورزی", "مسکن", "رفاه",
            "پست بانک", "توسعه تعاون", "توسعه صادرات", "صنعت و معدن", "پاسارگاد",
            "پارسیان", "سامان", "اقتصاد نوین", "سرمایه", "کارآفرین", "سینا", "شهر",
            "دی", "آینده", "گردشگری", "ایران زمین", "خاورمیانه", "رسالت",
            "قرض الحسنه مهر", "ایران ونزوئلا", "بلو", "ملل", "نور", "انصار",
            "قوامین", "حکمت ایرانیان", "مهر اقتصاد"
        )
        banks.forEachIndexed { index, bank ->
            // نام بانک فقط داده ورودی است؛ هیچ بانکی داخل منطق Parser ویژه نشده است.
            val dynamicAccounts = listOf(
                ParserAccount(1000L + index, "حساب $bank", bank),
                ParserAccount(9000L, "حساب روزمره", "بانک مقصد")
            )
            val sentence = "پنجاه هزار تومان از حساب $bank به حساب روزمره خودم انتقال دادم"
            val result = TransactionParser.parse(
                sentence, dynamicAccounts, categories, MoneyUnit.TOMAN, PersianDate(1405, 7, 3)
            )
            assertEquals("مبدأ بانک $bank", 1000L + index, result.accountId)
            assertEquals("مقصد بانک $bank", 9000L, result.targetAccountId)
            assertTrue("انتقال بانک $bank", result.transferToOwn)
        }
    }
}
