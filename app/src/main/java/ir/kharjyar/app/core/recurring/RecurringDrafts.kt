package ir.kharjyar.app.core.recurring

import androidx.room.withTransaction
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.data.db.KharjYarDatabase
import ir.kharjyar.app.data.db.RecurringOccurrenceEntity
import ir.kharjyar.app.data.db.TransactionEntity
import ir.kharjyar.app.data.db.TxSource
import ir.kharjyar.app.data.db.TxStatus
import java.time.Instant

/** A monthly schedule keeps its original Jalali day after shorter months. */
fun nextMonthlyOccurrence(at: Long, anchorDay: Int): Long {
    val zoned = Instant.ofEpochMilli(at).atZone(PersianDate.TEHRAN)
    val nextMonth = PersianDate.fromMillis(at).firstOfMonth().plusMonths(1)
    val date = PersianDate(nextMonth.year, nextMonth.month, anchorDay.coerceIn(1, nextMonth.monthLength()))
    return PersianDate.toMillis(date, zoned.hour, zoned.minute)
}

/** Idempotent under concurrent startup/worker runs; drafts do not affect account balances. */
suspend fun createDueRecurringDrafts(db: KharjYarDatabase, now: Long = System.currentTimeMillis()) {
    val dao = db.recurringTransactionDao()
    dao.due(now).forEach { candidate ->
        db.withTransaction {
            var schedule = dao.byId(candidate.id) ?: return@withTransaction
            val account = db.accountDao().byId(schedule.accountId)
            if (!schedule.enabled || account == null || account.archived) return@withTransaction
            var processed = 0
            while (schedule.nextAt <= now && processed++ < 24) {
                val due = schedule.nextAt
                if (!dao.hasOccurrence(schedule.id, due)) {
                    val id = db.transactionDao().insert(TransactionEntity(accountId = schedule.accountId,
                        amountRial = schedule.amountRial, direction = schedule.direction, nature = schedule.nature,
                        categoryId = schedule.categoryId, description = schedule.description, occurredAt = due,
                        recordedAt = now, source = TxSource.MANUAL, status = TxStatus.PENDING))
                    dao.insertOccurrence(RecurringOccurrenceEntity(recurringId = schedule.id, dueAt = due, transactionId = id))
                }
                schedule = schedule.copy(nextAt = nextMonthlyOccurrence(due, schedule.anchorDay))
                dao.update(schedule)
            }
        }
    }
}
