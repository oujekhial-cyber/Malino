package ir.kharjyar.app

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class FirstBalanceSmsGuardTest {
    @Test fun `first balance is anchored once while old accounts remain unchanged`() {
        val repository = File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
        val db = File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
        val backup = File("src/main/java/ir/kharjyar/app/core/backup/BackupPayload.kt").readText()
        assertTrue(repository.contains("account.firstBalanceSmsId == 0L"))
        assertTrue(repository.contains("releasePendingBalanceAnchor(tx)"))
        assertTrue(repository.contains("AccountNumberMatcher.match"))
        assertTrue(repository.contains("BankSmsSemanticType.BALANCE_ONLY"))
        assertTrue(db.contains("MIGRATION_26_27"))
        assertTrue(backup.contains("firstBalanceSmsId = e.firstBalanceSmsId"))
    }
}
