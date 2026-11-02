package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NotificationDuplicateResolutionGuardTest {
    @Test fun `opening stale sms notification resolves to confirmed transaction`() {
        val repo=File("src/main/java/ir/kharjyar/app/data/Repository.kt").readText()
        val dao=File("src/main/java/ir/kharjyar/app/data/db/Daos.kt").readText()
        val screen=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionEditScreen.kt").readText()
        assertTrue(dao.contains("findConfirmedDuplicateExcluding")&&dao.contains("id != :excludeId AND status = 1"))
        assertTrue(repo.contains("suspend fun resolvePendingDuplicate")&&repo.contains("txDao.delete(pending.id)"))
        assertTrue(repo.contains("status=SmsStatus.DONE"))
        assertTrue(screen.contains("viewModel.repo.resolvePendingDuplicate(txId)"))
        assertTrue(screen.contains("این تراکنش قبلاً ثبت شده است"))
    }
}
