package ir.kharjyar.app

import ir.kharjyar.app.core.backup.BTransaction
import ir.kharjyar.app.core.backup.BackupPayload
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxStatus
import ir.kharjyar.app.ui.screens.withTransferCounterparts
import org.junit.Assert.*
import org.junit.Test

class FinancialSafetyRegressionTest {
    private fun tx(id: Long, group: Long? = null, deleted: Long? = null, status: Int = TxStatus.CONFIRMED, sms: Long? = null) =
        TransactionEntity(id = id, accountId = 1, amountRial = 100, direction = TxDirection.DEPOSIT,
            occurredAt = 100, recordedAt = 100, transferGroupId = group, deletedAt = deleted, status = status, smsId = sms)

    @Test fun `backup round trips trash and old backup defaults to active`() {
        val item = BTransaction.of(tx(7, deleted = 555))
        val backup = BackupPayload(createdAt = 1, transactions = listOf(item))
        val json = backup.toJson()
        assertEquals(555L, BackupPayload.fromJson(json).transactions.single().toEntity().deletedAt)
        val oldJson = json.replace(",\"deletedAt\":555", "")
        assertNull(BackupPayload.fromJson(oldJson).transactions.single().toEntity().deletedAt)
    }

    @Test fun `transfer choice includes active counterpart even outside current filter`() {
        val selected = tx(1, group = 9)
        val pair = tx(2, group = 9)
        val deleted = tx(3, group = 9, deleted = 10)
        val unrelated = tx(4, group = 10)
        assertEquals(listOf(1L, 2L), withTransferCounterparts(listOf(selected), listOf(selected,pair,deleted,unrelated)).map { it.id })
        assertEquals(listOf(1L, 2L), withTransferCounterparts(listOf(selected,pair), listOf(selected,pair)).map { it.id })
    }

    @Test fun `pending duplicate anchor is released once before confirmation`() {
        val draft = tx(4, status = TxStatus.PENDING)
        val marker = AccountBalance.pendingTransactionAnchor(draft.id)
        assertTrue(AccountBalance.anchorMatchesPending(marker, draft))
        assertFalse(AccountBalance.anchorMatchesPending(-1, draft))
        assertFalse(AccountBalance.anchorMatchesPending(marker, draft.copy(status = TxStatus.CONFIRMED)))
        val account = AccountEntity(id = 1, title = "a", bankName = "b", createdAt = 0, programBalanceOffsetRial = 1000)
        assertEquals(1000L, AccountBalance.estimate(account, listOf(draft)).rial)
        val released = account.copy(programBalanceOffsetRial = AccountBalance.releaseAnchorOffset(1000, draft))
        assertEquals(1000L, AccountBalance.estimate(released, listOf(draft.copy(status = TxStatus.CONFIRMED))).rial)
        assertEquals(900L, AccountBalance.estimate(released, emptyList()).rial)
        assertTrue(AccountBalance.anchorMatchesPending(12, draft.copy(smsId = 12)))
    }
}
