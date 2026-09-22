package ir.kharjyar.app

import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.balance.BalanceSource
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountBalanceTest {

    private fun account(initial: Long? = null, at: Long? = null) = AccountEntity(
        id = 1,
        title = "جاری",
        bankName = "ملت",
        initialBalanceRial = initial,
        initialBalanceAt = at,
        createdAt = 0
    )

    private fun tx(
        id: Long,
        amount: Long,
        direction: Int,
        at: Long,
        status: Int = TxStatus.CONFIRMED,
        balanceAfter: Long? = null,
        accountId: Long = 1
    ) = TransactionEntity(
        id = id,
        accountId = accountId,
        amountRial = amount,
        direction = direction,
        occurredAt = at,
        recordedAt = at,
        status = status,
        balanceAfterRial = balanceAfter
    )

    @Test
    fun noBasis_returnsNone() {
        val e = AccountBalance.estimate(account(), emptyList())
        assertNull(e.rial)
        assertEquals(BalanceSource.NONE, e.source)
    }

    @Test
    fun initialPlusConfirmedTransactions() {
        val e = AccountBalance.estimate(
            account(initial = 1_000_000, at = 100),
            listOf(
                tx(1, 200_000, TxDirection.DEPOSIT, 200),
                tx(2, 50_000, TxDirection.WITHDRAW, 300)
            )
        )
        assertEquals(BalanceSource.INITIAL_PLUS_TX, e.source)
        assertEquals(1_150_000L, e.rial)
        assertEquals(100L, e.asOf)
    }

    @Test
    fun pendingTransactionsAreExcluded_butCounted() {
        val e = AccountBalance.estimate(
            account(initial = 1_000_000, at = 100),
            listOf(
                tx(1, 200_000, TxDirection.DEPOSIT, 200),
                tx(2, 999_000, TxDirection.WITHDRAW, 300, status = TxStatus.PENDING)
            )
        )
        assertEquals(1_200_000L, e.rial)
        assertEquals(1, e.pendingCount)
    }

    @Test
    fun newerSmsBalanceWins_andLaterTxAreAdded() {
        val e = AccountBalance.estimate(
            account(initial = 1_000_000, at = 100),
            listOf(
                tx(1, 300_000, TxDirection.WITHDRAW, 500, balanceAfter = 2_000_000),
                tx(2, 100_000, TxDirection.DEPOSIT, 600)
            )
        )
        assertEquals(BalanceSource.SMS_BALANCE, e.source)
        assertEquals(2_100_000L, e.rial)
        assertEquals(500L, e.asOf)
    }

    @Test
    fun olderSmsBalanceLosesToNewerInitialBalance() {
        val e = AccountBalance.estimate(
            account(initial = 5_000_000, at = 900),
            listOf(tx(1, 300_000, TxDirection.WITHDRAW, 500, balanceAfter = 2_000_000))
        )
        assertEquals(BalanceSource.INITIAL_PLUS_TX, e.source)
        assertEquals(5_000_000L, e.rial)
    }

    @Test
    fun otherAccountsAreIgnored() {
        val e = AccountBalance.estimate(
            account(initial = 1_000_000, at = 0),
            listOf(tx(1, 500_000, TxDirection.WITHDRAW, 200, accountId = 2))
        )
        assertEquals(1_000_000L, e.rial)
    }

    @Test
    fun smsBalanceWithoutInitial_works() {
        val e = AccountBalance.estimate(
            account(),
            listOf(tx(1, 10_000, TxDirection.WITHDRAW, 400, balanceAfter = 750_000))
        )
        assertEquals(BalanceSource.SMS_BALANCE, e.source)
        assertEquals(750_000L, e.rial)
    }
}
