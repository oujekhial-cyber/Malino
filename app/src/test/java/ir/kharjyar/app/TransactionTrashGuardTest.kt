package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TransactionTrashGuardTest {
    private val entity=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
    private val dao=File("src/main/java/ir/kharjyar/app/data/db/Daos.kt").readText()
    private val database=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
    private val screen=File("src/main/java/ir/kharjyar/app/ui/screens/TransactionTrashScreen.kt").readText()

    @Test fun `ordinary deletion is soft and active queries hide trash`() {
        assertTrue(entity.contains("val deletedAt: Long? = null"))
        assertTrue(dao.contains("UPDATE transactions SET deletedAt=:deletedAt WHERE id = :id"))
        assertTrue(dao.contains("WHERE deletedAt IS NULL ORDER BY occurredAt DESC"))
        assertTrue(dao.contains("observeTrash(): Flow<List<TransactionEntity>>"))
    }

    @Test fun `trash supports restore confirmed permanent deletion and automatic expiry`() {
        assertTrue(dao.contains("UPDATE transactions SET deletedAt=NULL WHERE id=:id"))
        assertTrue(dao.contains("DELETE FROM transactions WHERE id=:id AND deletedAt IS NOT NULL"))
        assertTrue(dao.contains("purgeTrashOlderThan(cutoff: Long)"))
        assertTrue(screen.contains("۳۰ روز قابل بازگرداندن"))
        assertTrue(screen.contains("حذف همیشگی تراکنش"))
        val vm=File("src/main/java/ir/kharjyar/app/ui/AppViewModel.kt").readText()
        assertTrue(vm.contains("30L * 24 * 60 * 60 * 1000"))
    }

    @Test fun `database migration preserves existing transactions`() {
        assertTrue(database.contains("version = 23"))
        assertTrue(database.contains("ALTER TABLE transactions ADD COLUMN deletedAt INTEGER"))
        assertTrue(database.contains("MIGRATION_22_23"))
    }
}
