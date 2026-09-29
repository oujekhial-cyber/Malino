package ir.kharjyar.app.core.balance

import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.data.db.TxStatus

/**
 * جمع واریز/برداشت یک مجموعه تراکنش.
 *
 * منطق خالص و بدون دیتابیس است تا صفحه خانه بتواند دقیقاً همان فهرستی را
 * خلاصه کند که کارت‌ها و نمودار نشان می‌دهند. قبلاً این خلاصه با یک کوئری جدا
 * و «تازه‌سازی دستی» ساخته می‌شد و بعد از بازیابی بکاپ یا تعویض کارت،
 * گاهی به‌روز نمی‌شد و فیلتر حساب را اعمال نمی‌کرد.
 */
data class TxSummary(
    val incomeRial: Long = 0,
    val expenseRial: Long = 0,
    val pendingCount: Int = 0,
    val pendingIncomeRial: Long = 0,
    val pendingExpenseRial: Long = 0
) {
    val netRial: Long get() = incomeRial - expenseRial
    val isEmpty: Boolean
        get() = incomeRial == 0L && expenseRial == 0L && pendingCount == 0
}

object TxSummarizer {

    /**
     * @param accountId اگر null باشد همه حساب‌ها، وگرنه فقط همین حساب.
     * @param from شروع بازه (شامل)؛ null یعنی بدون محدودیت زمانی.
     * @param to پایان بازه (غیرشامل)؛ null یعنی بدون محدودیت زمانی.
     */
    fun summarize(
        txs: List<TransactionEntity>,
        accountId: Long? = null,
        from: Long? = null,
        to: Long? = null
    ): TxSummary {
        var income = 0L
        var expense = 0L
        var pendingIncome = 0L
        var pendingExpense = 0L
        var pendingCount = 0

        for (t in txs) {
            if (accountId != null && t.accountId != accountId) continue
            if (from != null && t.occurredAt < from) continue
            if (to != null && t.occurredAt >= to) continue
            // انتقال بین حساب‌های خود کاربر نه درآمد است نه خرج
            if (t.nature == TxNature.TRANSFER) continue

            val isIncome = t.nature == TxNature.INCOME ||
                (t.nature == TxNature.UNKNOWN && t.direction == TxDirection.DEPOSIT)
            val isExpense = t.nature == TxNature.EXPENSE ||
                (t.nature == TxNature.UNKNOWN && t.direction == TxDirection.WITHDRAW)
            val confirmed = t.status == TxStatus.CONFIRMED

            when {
                isIncome && confirmed -> income += t.amountRial
                isIncome -> { pendingIncome += t.amountRial; pendingCount++ }
                isExpense && confirmed -> expense += t.amountRial
                isExpense -> { pendingExpense += t.amountRial; pendingCount++ }
            }
        }

        return TxSummary(income, expense, pendingCount, pendingIncome, pendingExpense)
    }
}
