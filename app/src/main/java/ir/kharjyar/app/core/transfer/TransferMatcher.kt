package ir.kharjyar.app.core.transfer

/**
 * تطبیق دو سمت انتقال داخلی بین حساب‌های خود کاربر.
 * دو تراکنش با مبلغ برابر، جهت مخالف، حساب متفاوت و فاصله زمانی کم کاندید یک گروه انتقال هستند.
 * در حالت مبهم (چند کاندید) تأیید کاربر لازم است.
 */
data class TransferCandidate(
    val txId: Long,
    val accountId: Long,
    val amountRial: Long,
    val direction: Int, // 0 = deposit, 1 = withdraw
    val occurredAt: Long
)

sealed class TransferMatch {
    data class Matched(val counterpartTxId: Long) : TransferMatch()
    data class Ambiguous(val candidateTxIds: List<Long>) : TransferMatch()
    object None : TransferMatch()
}

object TransferMatcher {

    const val DEFAULT_WINDOW_MILLIS = 30 * 60 * 1000L // ۳۰ دقیقه

    fun match(
        tx: TransferCandidate,
        openCandidates: List<TransferCandidate>,
        windowMillis: Long = DEFAULT_WINDOW_MILLIS
    ): TransferMatch {
        val oppositeDirection = if (tx.direction == 0) 1 else 0
        val matches = openCandidates.filter {
            it.txId != tx.txId &&
                it.accountId != tx.accountId &&
                it.amountRial == tx.amountRial &&
                it.direction == oppositeDirection &&
                kotlin.math.abs(it.occurredAt - tx.occurredAt) <= windowMillis
        }
        return when {
            matches.isEmpty() -> TransferMatch.None
            matches.size == 1 -> TransferMatch.Matched(matches[0].txId)
            else -> TransferMatch.Ambiguous(matches.map { it.txId })
        }
    }
}
