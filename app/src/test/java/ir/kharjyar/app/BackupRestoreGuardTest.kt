package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * نگهبان باگ «از بین رفتن صف بررسی هنگام بازیابی».
 * بازیابی همچنان جایگزینی کامل است، ولی باید پیش از پاک‌سازی، صف بررسی‌نشده را
 * بردارد و با صف بکاپ ادغام کند.
 */
class BackupRestoreGuardTest {

    private val source =
        File("src/main/java/ir/kharjyar/app/core/backup/BackupManager.kt").readText()

    @Test
    fun `queue is read before the tables are cleared`() {
        val restore = source.substringAfter("suspend fun restore(")
        val readIndex = restore.indexOf("listByStatus")
        val clearIndex = restore.indexOf("clearAllTablesInTransaction")
        assertTrue("صف بررسی باید خوانده شود", readIndex >= 0)
        assertTrue("پاک‌سازی باید بعد از خواندن صف باشد", clearIndex > readIndex)
    }

    @Test
    fun `carried over rows are inserted after the backup queue`() {
        val restore = source.substringAfter("suspend fun restore(")
        val backupInsert = restore.indexOf("payload.smsQueue.forEach")
        val carriedInsert = restore.indexOf("merge.carriedOver.forEach")
        assertTrue(backupInsert >= 0 && carriedInsert > backupInsert)
    }

    @Test
    fun `merge is driven by the fingerprint helper`() {
        assertTrue(source.contains("SmsQueueMerge.merge"))
        assertTrue(source.contains("SmsQueueMerge.UNREVIEWED_STATUSES"))
    }

    @Test
    fun `whole restore still happens inside one transaction`() {
        assertTrue(source.contains("db.withTransaction {"))
    }
}
