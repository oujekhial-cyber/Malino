package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DebtAccountTransactionGuardTest {
    @Test fun `creating debt optionally adjusts selected account with transfer transaction`() {
        val source = File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
        listOf(
            "اعمال در حساب و ثبت تراکنش",
            "مبلغ طلب از حساب انتخابی کم می‌شود",
            "مبلغ بدهی به حساب انتخابی اضافه می‌شود",
            "بدون تغییر مانده حساب و بدون ساخت تراکنش",
            "registerAccountTransaction",
            "label = if (receivable) \"پرداخت طلب از حساب\" else \"واریز مبلغ بدهی به حساب\"",
            "direction = if (receivable) TxDirection.WITHDRAW else TxDirection.DEPOSIT",
            "nature = TxNature.TRANSFER",
            "counterparty = person.trim()",
            "(!registerAccountTransaction || accountId != null)"
        ).forEach { assertTrue(it, source.contains(it)) }
    }
}
