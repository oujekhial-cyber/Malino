package ir.kharjyar.app

import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.AccountType
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxStatus
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CashAccountIntegrationGuardTest {
    @Test fun `cash fund balance uses initial cash plus confirmed inflows and outflows`() {
        val cash = AccountEntity(id=7,title="کیف پول من",bankName="",accountType=AccountType.CASH,ownerName="من",cashLocation="کیف پول",initialBalanceRial=1_000_000,initialBalanceAt=100,createdAt=100)
        val tx = listOf(
            TransactionEntity(id=1,accountId=7,amountRial=400_000,direction=TxDirection.DEPOSIT,status=TxStatus.CONFIRMED,occurredAt=110,recordedAt=110),
            TransactionEntity(id=2,accountId=7,amountRial=250_000,direction=TxDirection.WITHDRAW,status=TxStatus.CONFIRMED,occurredAt=120,recordedAt=120)
        )
        assertEquals(1_150_000L, AccountBalance.estimate(cash,tx).rial)
    }

    @Test fun `cash account is integrated into model backup migration and account management`() {
        val entity=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
        val backup=File("src/main/java/ir/kharjyar/app/core/backup/BackupPayload.kt").readText()
        val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
        val form=File("src/main/java/ir/kharjyar/app/ui/screens/AccountEditScreen.kt").readText()
        val settings=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
        listOf("accountType","ownerName","cashLocation","note").forEach { assertTrue(entity.contains(it) && backup.contains(it)) }
        assertTrue(db.contains("version = 16")); assertTrue(db.contains("MIGRATION_13_14"))
        listOf("صندوق نقدی","صاحب صندوق","محل نگهداری","accountType == AccountType.BANK").forEach { assertTrue(form.contains(it)) }
        assertTrue(settings.contains("account.accountType==AccountType.CASH"))
    }
}
