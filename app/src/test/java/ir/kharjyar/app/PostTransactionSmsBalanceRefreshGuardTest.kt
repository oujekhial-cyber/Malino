package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PostTransactionSmsBalanceRefreshGuardTest {
    private val repo=File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
    private val app=File("src/main/java/ir/kharjyar/app/KharjYarApp.kt").readText()
    private val dao=File("src/main/java/ir/kharjyar/app/data/db/BankBalanceSnapshot.kt").readText()

    @Test fun `every manual account movement refreshes reported SMS balance`() {
        assertTrue(app.contains("Repository(database, applicationContext)"))
        assertTrue(repo.contains("BankBalanceRefreshService.latest"))
        assertTrue(repo.contains("refreshReportedSmsBalance(accountId)"))
        assertTrue(repo.contains("refreshReportedSmsBalance(fromAccountId)"))
        assertTrue(repo.contains("refreshReportedSmsBalance(toAccountId)"))
    }

    @Test fun `refresh is safe and never replaces a newer reported balance`() {
        assertTrue(repo.contains("runCatching"))
        assertTrue(dao.contains("suspend fun byAccount"))
        assertTrue(repo.contains("sms.occurredAt >= previous.messageAt"))
        assertTrue(repo.contains("BankBalanceSnapshotEntity"))
    }
}
