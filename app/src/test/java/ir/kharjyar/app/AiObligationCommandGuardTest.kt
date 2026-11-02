package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiObligationCommandGuardTest {
    private val ai=File("src/main/java/ir/kharjyar/app/ai/ExperimentalPersianAi.kt").readText()
    private val quick=File("src/main/java/ir/kharjyar/app/ui/screens/QuickAddScreen.kt").readText()
    private val loans=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
    private val debts=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()

    @Test fun `loan and debt commands are not flattened to income or expense`() {
        listOf("loan_borrowed","loan_lent","debt_receivable","debt_payable").forEach { assertTrue(ai.contains(it)) }
        assertTrue(ai.contains("اصل وام، قرض، خرید دارایی، پرداخت قسط و انتقال‌ها هرگز income یا expense عادی نیستند"))
        assertTrue(quick.contains("prepareObligationDraft"))
    }

    @Test fun `obligation forms receive draft and create neutral linked account movement`() {
        assertTrue(loans.contains("pendingObligationDraft"))
        assertTrue(loans.contains("nature=TxNature.NEUTRAL"))
        assertTrue(loans.contains("ثبت اصل وام در حساب"))
        assertTrue(debts.contains("pendingObligationDraft"))
        assertTrue(debts.contains("nature = TxNature.TRANSFER"))
    }

    @Test fun `missing account requires explicit local selection`() {
        assertTrue(quick.contains("از کدام حساب پرداخت کردید؟"))
        assertTrue(quick.contains("مبلغ به کدام حساب واریز شده؟"))
        assertTrue(quick.contains("ComboBox"))
    }
}
