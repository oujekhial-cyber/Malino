package ir.kharjyar.app

import ir.kharjyar.app.data.db.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ObligationTrashGuardTest {
    @Test fun `backup roundtrip preserves deleted timestamps and old backups default to active`() {
        val json = Json { encodeDefaults = true }
        val person = DebtPersonEntity(id = 1, name = "علی", createdAt = 42, deletedAt = 123)
        assertEquals(person, json.decodeFromString<DebtPersonEntity>(json.encodeToString(person)))
        assertNull(Json.decodeFromString<DebtPersonEntity>("""{"id":1,"name":"علی","createdAt":42}""").deletedAt)
        val loan = LoanEntity(title = "وام", party = "علی", kind = 0, principalRial = 100, installmentAmountRial = 50, installmentCount = 2, startAt = 1, nextDueAt = 2, deletedAt = 123)
        assertEquals(loan, json.decodeFromString<LoanEntity>(json.encodeToString(loan)))
    }
    @Test fun `migration and all routes use retained children and periodic purge`() {
        val db = File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
        val debt = File("src/main/java/ir/kharjyar/app/data/db/ObligationDaos.kt").readText()
        val loans = File("src/main/java/ir/kharjyar/app/data/db/PlanningDaos.kt").readText()
        val worker = File("src/main/java/ir/kharjyar/app/work/ObligationTrash.kt").readText()
        assertTrue(db.contains("MIGRATION_24_25"))
        assertTrue(db.contains("ADD COLUMN `deletedAt` INTEGER"))
        assertTrue(debt.contains("personId IN (SELECT id FROM debt_people WHERE deletedAt IS NULL)"))
        assertTrue(loans.contains("loanId IN (SELECT id FROM loans WHERE deletedAt IS NULL)"))
        assertTrue(worker.contains("30L * 24 * 60 * 60 * 1000"))
        assertTrue(worker.contains("db.withTransaction"))
        assertTrue(File("src/main/java/ir/kharjyar/app/work/ObligationReminderWorker.kt").readText().contains("ObligationTrash.purgeExpired"))
    }
}
