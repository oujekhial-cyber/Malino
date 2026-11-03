package ir.kharjyar.app.work

import android.content.Context
import androidx.room.withTransaction
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.data.db.*

/** Retains children and identifiers until explicit deletion or expiry. */
object ObligationTrash {
    const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000

    suspend fun purgeExpired(db: KharjYarDatabase, now: Long = System.currentTimeMillis()) {
        val cutoff = now - RETENTION_MS
        db.withTransaction {
            val debtDao = db.debtDao()
            debtDao.expiredDebts(cutoff).forEach { debtDao.permanentlyDeleteDebt(it) }
            debtDao.expiredPeople(cutoff).forEach { debtDao.deletePersonWithDebts(it.id) }
            db.loanDao().expired(cutoff).forEach { db.loanDao().permanentlyDelete(it.id) }
            db.checkDao().expired(cutoff).forEach { db.checkDao().permanentlyDelete(it.id) }
        }
    }

    /** Recreate only future check reminders; cancelled past notifications stay cancelled. */
    suspend fun restoreCheck(context: Context, db: KharjYarDatabase, check: CheckEntity) {
        db.checkDao().restore(check.id)
        if (check.status != CheckStatus.PENDING) return
        val due = PersianDate.fromMillis(check.dueAt)
        val number = check.sayadId.ifBlank { check.serialNumber }
        val note = "چک ${if (check.direction == CheckDirection.RECEIVED) "دریافتی" else "صادرشده"} شماره $number — طرف حساب: ${check.counterparty}"
        listOf("یادآوری چک؛ یک روز مانده به سررسید" to due.plusDays(-1), "موعد چک" to due).forEach { (title, date) ->
            val at = date.startOfDayMillis() + 9 * 60 * 60 * 1000L
            if (at > System.currentTimeMillis()) {
                val id = db.reminderDao().insert(ReminderEntity(title = title, category = "چک", note = note, nextAt = at))
                LifeReminderWorker.schedule(context, id, at)
            }
        }
    }
}
