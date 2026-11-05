package ir.kharjyar.app.work

import android.content.Context
import androidx.room.withTransaction
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.data.db.*
import java.io.File

/** Retains children and identifiers until explicit deletion or expiry. */
object ObligationTrash {
    const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000

    suspend fun purgeExpired(context: Context, db: KharjYarDatabase, now: Long = System.currentTimeMillis()) {
        val cutoff = now - RETENTION_MS
        val deletedChecks = db.withTransaction {
            val debtDao = db.debtDao()
            debtDao.expiredDebts(cutoff).forEach { debtDao.permanentlyDeleteDebt(it) }
            debtDao.expiredPeople(cutoff).forEach { debtDao.deletePersonWithDebts(it.id) }
            db.loanDao().expired(cutoff).forEach { db.loanDao().permanentlyDelete(it.id) }
            db.checkDao().expired(cutoff).filter { db.checkDao().permanentlyDelete(it.id) > 0 }
        }
        deletedChecks.forEach { removeUnusedCheckImage(context, db, it.imagePath) }
    }

    suspend fun permanentlyDeleteCheck(context: Context, db: KharjYarDatabase, check: CheckEntity) {
        if (db.checkDao().permanentlyDelete(check.id) > 0) removeUnusedCheckImage(context, db, check.imagePath)
    }

    private suspend fun removeUnusedCheckImage(context: Context, db: KharjYarDatabase, path: String) {
        if (path.isBlank()) return
        if (db.checkDao().allOnce().any { it.imagePath == path } ||
            db.transactionAttachmentDao().allOnce().any { it.imagePath == path } ||
            db.civicDao().allProfilesOnce().any { it.imagePath == path }) return
        runCatching {
            val dir = File(context.filesDir, "checks").canonicalFile
            val file = File(path).canonicalFile
            if (file.parentFile == dir && file.isFile) file.delete()
        }
    }

    /** Recreate only future check reminders; cancelled past notifications stay cancelled. */
    suspend fun restoreCheck(context: Context, db: KharjYarDatabase, check: CheckEntity) {
        val created = db.withTransaction {
            if (db.checkDao().restore(check.id) == 0 || check.status != CheckStatus.PENDING) return@withTransaction emptyList<Pair<Long, Long>>()
            val due = PersianDate.fromMillis(check.dueAt)
            val number = check.sayadId.ifBlank { check.serialNumber }
            val note = "چک ${if (check.direction == CheckDirection.RECEIVED) "دریافتی" else "صادرشده"} شماره $number — طرف حساب: ${check.counterparty}"
            listOf("یادآوری چک؛ یک روز مانده به سررسید" to due.plusDays(-1), "موعد چک" to due).mapNotNull { (title, date) ->
                val at = date.startOfDayMillis() + 9 * 60 * 60 * 1000L
                if (at <= System.currentTimeMillis()) null else {
                    val id = db.reminderDao().insert(ReminderEntity(title = title, category = "چک", note = note, nextAt = at))
                    id to at
                }
            }
        }
        created.forEach { (id, at) -> LifeReminderWorker.schedule(context, id, at) }
    }
}
