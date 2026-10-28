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
        assertTrue(repo.contains("refreshAccountBalanceSnapshot(accountId)"))
        assertTrue(repo.contains("refreshAccountBalanceSnapshot(fromAccountId)"))
        assertTrue(repo.contains("refreshAccountBalanceSnapshot(toAccountId)"))
        assertTrue(app.contains("observeTransactionsForBalanceRefresh()"))
        assertTrue(app.contains("repository.txDao.observeAll()"))
        assertTrue(app.contains("affectedAccounts.forEach { repository.refreshAccountBalanceSnapshot(it) }"))
    }

    @Test fun `refresh is safe and never replaces a newer reported balance`() {
        assertTrue(repo.contains("runCatching"))
        assertTrue(dao.contains("suspend fun byAccount"))
        assertTrue(repo.contains("sms.occurredAt >= previous.messageAt"))
        assertTrue(repo.contains("BankBalanceSnapshotEntity"))
    }

    @Test fun `account card explicitly compares app and sms balances`() {
        val dashboard=File("src/main/java/ir/kharjyar/app/ui/screens/DashboardScreen.kt").readText()
        assertTrue(dashboard.contains("مانده برنامه: \$appBalance • مانده پیامک: \$smsBalance"))
        assertTrue(dashboard.contains("مغایرت \${Money.format"))
        assertTrue(dashboard.contains("بدون مغایرت"))
    }
}
