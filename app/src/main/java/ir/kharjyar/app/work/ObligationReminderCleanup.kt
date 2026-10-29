package ir.kharjyar.app.work

import android.content.Context
import ir.kharjyar.app.data.db.CheckEntity
import ir.kharjyar.app.data.db.KharjYarDatabase
import ir.kharjyar.app.notify.Notifier

/** لغو اتمیکِ منطقیِ همه مسیرهای اعلان مرتبط با تعهد تسویه‌شده یا حذف‌شده. */
object ObligationReminderCleanup {
    suspend fun cancelCheck(context: Context, db: KharjYarDatabase, check: CheckEntity) {
        // اعلان ساخته‌شده توسط ObligationReminderWorker
        Notifier.cancelCheckReminder(context, check.id)

        // چک دو ReminderEntity دارد (یک روز قبل و روز سررسید). لغو Work به‌تنهایی
        // اعلان نمایش‌داده‌شده را پاک نمی‌کند؛ هر سه بخش باید انجام شوند.
        val number = check.sayadId.ifBlank { check.serialNumber }.trim()
        if (number.isNotEmpty()) {
            db.reminderDao().allOnce()
                .filter { it.category == "چک" && it.note.contains(number) }
                .forEach { reminder ->
                    LifeReminderWorker.cancel(context, reminder.id)
                    Notifier.cancelLifeReminder(context, reminder.id)
                    if (reminder.enabled) db.reminderDao().update(reminder.copy(enabled = false))
                }
        }
    }
}
