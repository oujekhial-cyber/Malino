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

    /** موجودی اولیه ثبت نشده و مانده از خالص تراکنش‌های قطعی برنامه محاسبه شده است. */
    TRANSACTIONS_ONLY,

    /** هیچ مبنایی و هیچ تراکنش قطعی وجود ندارد. */
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
        BalanceSource.TRANSACTIONS_ONLY -> "خالص تراکنش‌های تأییدشده برنامه"
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
     * مبنای ثابت مستقل از ماندهٔ پیامکی است تا افزودن، حذف و ویرایش
     * هر تراکنش قطعی، حتی تراکنش قدیمی، در مانده برنامه منعکس شود.
     */
    fun estimate(account: AccountEntity, transactions: List<TransactionEntity>): EstimatedBalance {
        val mine = transactions.filter { it.accountId == account.id && it.deletedAt == null }
        val confirmed = mine.filter { it.status == TxStatus.CONFIRMED }
        val pendingCount = mine.count { it.status != TxStatus.CONFIRMED }
        val offset = account.programBalanceOffsetRial
        if (offset != null) return EstimatedBalance(
            offset + confirmed.sumOf { signed(it) }, BalanceSource.INITIAL_PLUS_TX,
            account.initialBalanceAt, pendingCount
        )
        if (account.initialBalanceRial != null) {
            val from = account.initialBalanceAt ?: Long.MIN_VALUE
            return EstimatedBalance(account.initialBalanceRial + confirmed.filter { it.occurredAt >= from }.sumOf { signed(it) }, BalanceSource.INITIAL_PLUS_TX, account.initialBalanceAt, pendingCount)
        }
        if (confirmed.isNotEmpty()) return EstimatedBalance(
            confirmed.sumOf { signed(it) }, BalanceSource.TRANSACTIONS_ONLY,
            confirmed.minOfOrNull { it.occurredAt }, pendingCount
        )
        return EstimatedBalance(null, BalanceSource.NONE, null, pendingCount)
    }

    /** Freeze the old displayed balance while preserving the effect of every active transaction. */
    fun legacyOffset(account: AccountEntity, transactions: List<TransactionEntity>): Long? {
        val active = transactions.filter { it.accountId == account.id && it.deletedAt == null }
        val oldBalance = legacyEstimate(account, active).rial ?: return null
        return oldBalance - active.filter { it.status == TxStatus.CONFIRMED }.sumOf { signed(it) }
    }

    /** Previous algorithm, used only for migrating existing accounts and old backups. */
    private fun legacyEstimate(account: AccountEntity, transactions: List<TransactionEntity>): EstimatedBalance {
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

            confirmed.isNotEmpty() -> EstimatedBalance(
                confirmed.sumOf { signed(it) },
                BalanceSource.TRANSACTIONS_ONLY,
                confirmed.minOfOrNull { it.occurredAt },
                pendingCount
            )

            else -> EstimatedBalance(null, BalanceSource.NONE, null, pendingCount)
        }
    }
}
