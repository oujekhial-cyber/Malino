package ir.kharjyar.app.core.balance

import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxStatus

/** منبع محاسبه مانده برآوردی. */
enum class BalanceSource {
    /** موجودی اولیهٔ ثبت‌شده توسط کاربر + تراکنش‌های تأییدشده. */
    INITIAL_PLUS_TX,

    /** آخرین ماندهٔ اعلام‌شده در پیامک بانک + تراکنش‌های تأییدشدهٔ بعد از آن. */
    SMS_BALANCE,

    /** هیچ مبنایی وجود ندارد (نه موجودی اولیه، نه ماندهٔ پیامکی). */
    NONE
}

/**
 * ماندهٔ برآوردی یک حساب.
 * @param rial مقدار برآوردی به ریال (اگر [source] برابر NONE باشد، null).
 * @param asOf زمان مبنای محاسبه (زمان ثبت موجودی اولیه یا زمان پیامک مانده).
 * @param confirmedOnly همیشه true — تراکنش‌های تأییدنشده در محاسبه نمی‌آیند.
 */
data class EstimatedBalance(
    val rial: Long?,
    val source: BalanceSource,
    val asOf: Long?,
    val pendingCount: Int = 0,
    val confirmedOnly: Boolean = true
) {
    /** برچسب فارسی منبع، برای نمایش شفاف روی کارت داشبورد. */
    fun sourceLabel(): String = when (source) {
        BalanceSource.INITIAL_PLUS_TX -> "موجودی اولیه + تراکنش‌های تأییدشده"
        BalanceSource.SMS_BALANCE -> "آخرین مانده پیامک بانک + تراکنش‌های بعد"
        BalanceSource.NONE -> "مبنای محاسبه ثبت نشده"
    }
}

object AccountBalance {

    /** اثر یک تراکنش روی مانده: واریز مثبت، برداشت منفی. */
    private fun signed(tx: TransactionEntity): Long =
        if (tx.direction == TxDirection.DEPOSIT) tx.amountRial else -tx.amountRial

    /**
     * مانده برآوردی حساب:
     * موجودی اولیه + جمع تراکنش‌های تأییدشدهٔ بعد از زمان آن؛
     * اما اگر آخرین ماندهٔ اعلام‌شده در پیامک از موجودی اولیه جدیدتر باشد،
     * مبنا همان مانده پیامکی می‌شود و فقط تراکنش‌های تأییدشدهٔ بعد از آن اضافه می‌شوند.
     */
    fun estimate(account: AccountEntity, transactions: List<TransactionEntity>): EstimatedBalance {
        val mine = transactions.filter { it.accountId == account.id }
        val confirmed = mine.filter { it.status == TxStatus.CONFIRMED }
        val pendingCount = mine.count { it.status != TxStatus.CONFIRMED }

        val latestSms = confirmed
            .filter { it.balanceAfterRial != null }
            .maxByOrNull { it.occurredAt }

        val initialAt = account.initialBalanceAt
        val initialRial = account.initialBalanceRial

        val useSms = latestSms != null &&
            (initialRial == null || initialAt == null || latestSms.occurredAt >= initialAt)

        return when {
            useSms -> {
                val base = latestSms!!.balanceAfterRial!!
                val after = confirmed
                    .filter { it.occurredAt > latestSms.occurredAt || (it.occurredAt == latestSms.occurredAt && it.id > latestSms.id) }
                    .sumOf { signed(it) }
                EstimatedBalance(base + after, BalanceSource.SMS_BALANCE, latestSms.occurredAt, pendingCount)
            }

            initialRial != null -> {
                val from = initialAt ?: Long.MIN_VALUE
                val delta = confirmed.filter { it.occurredAt >= from }.sumOf { signed(it) }
                EstimatedBalance(initialRial + delta, BalanceSource.INITIAL_PLUS_TX, initialAt, pendingCount)
            }

            else -> EstimatedBalance(null, BalanceSource.NONE, null, pendingCount)
        }
    }
}
