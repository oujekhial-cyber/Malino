package ir.kharjyar.app

import ir.kharjyar.app.core.backup.BackupPayload
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.recurring.nextMonthlyOccurrence
import ir.kharjyar.app.data.db.RecurringTransactionEntity
import ir.kharjyar.app.data.db.RecurringOccurrenceEntity
import ir.kharjyar.app.data.prefs.parseHomeShortcuts
import org.junit.Assert.*
import org.junit.Test

class RecurringAndHomeFeaturesTest {
    @Test fun `monthly schedule returns to original day after short month`() {
        val at = PersianDate.toMillis(PersianDate(1403,6,31), 9, 0)
        val next = nextMonthlyOccurrence(at, 31)
        assertEquals(PersianDate(1403,7,30), PersianDate.fromMillis(next))
        val spring = nextMonthlyOccurrence(PersianDate.toMillis(PersianDate(1403,12,30),9,0),31)
        assertEquals(PersianDate(1404,1,31), PersianDate.fromMillis(spring))
    }
    @Test fun `recurring schedules and occurrence identity round trip in backup`() {
        val schedule = RecurringTransactionEntity(id=1,accountId=2,amountRial=100,direction=1,nature=2,nextAt=1000,anchorDay=31)
        val occurrence = RecurringOccurrenceEntity(id=4,recurringId=1,dueAt=900,transactionId=7)
        val payload = BackupPayload(createdAt=10,recurringTransactions=listOf(schedule),recurringOccurrences=listOf(occurrence))
        val restored = BackupPayload.fromJson(payload.toJson())
        assertEquals(schedule, restored.recurringTransactions.single())
        assertEquals(occurrence, restored.recurringOccurrences.single())
        assertTrue(BackupPayload.fromJson("{\"createdAt\":1}").recurringTransactions.isEmpty())
    }
    @Test fun `shortcut preferences preserve order and reject unknown destinations`() {
        assertEquals(listOf("globalSearch", "manual"), parseHomeShortcuts("globalSearch,invalid,manual,globalSearch"))
        assertTrue(parseHomeShortcuts("").isEmpty())
    }
}
