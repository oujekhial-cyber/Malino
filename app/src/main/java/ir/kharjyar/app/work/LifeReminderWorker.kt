package ir.kharjyar.app.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.data.db.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class LifeReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as KharjYarApp
        val id = inputData.getLong(KEY_ID, 0)
        val rows = if (id > 0) listOfNotNull(app.database.reminderDao().get(id)) else app.database.reminderDao().due(System.currentTimeMillis())
        rows.filter { it.enabled && it.nextAt <= System.currentTimeMillis() + 60_000 }.forEach { reminder ->
            if (android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(applicationContext).notify((12000 + reminder.id).toInt(), NotificationCompat.Builder(applicationContext, KharjYarApp.CHANNEL_REVIEW).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(reminder.title).setContentText(reminder.note.ifBlank { reminder.category }).setStyle(NotificationCompat.BigTextStyle().bigText(reminder.note.ifBlank { reminder.category })).setAutoCancel(true).build())
            }
            val next = nextOccurrence(reminder)
            app.database.reminderDao().update(reminder.copy(nextAt = next ?: reminder.nextAt, enabled = next != null, lastNotifiedAt = System.currentTimeMillis()))
            if (next != null) schedule(applicationContext, reminder.id, next)
        }
        return Result.success()
    }

    companion object {
        private const val KEY_ID = "reminderId"
        fun schedule(context: Context, id: Long, at: Long) {
            val request = OneTimeWorkRequestBuilder<LifeReminderWorker>().setInitialDelay((at - System.currentTimeMillis()).coerceAtLeast(0), TimeUnit.MILLISECONDS).setInputData(workDataOf(KEY_ID to id)).build()
            WorkManager.getInstance(context).enqueueUniqueWork("life-reminder-$id", ExistingWorkPolicy.REPLACE, request)
        }
        fun cancel(context: Context, id: Long) = WorkManager.getInstance(context).cancelUniqueWork("life-reminder-$id")
        fun nextOccurrence(r: ReminderEntity): Long? {
            if (r.repeatType == ReminderRepeat.ONCE) return null
            val z = ZonedDateTime.ofInstant(Instant.ofEpochMilli(r.nextAt), ZoneId.systemDefault())
            val n = r.repeatInterval.coerceAtLeast(1).toLong()
            return when (r.repeatType) {
                ReminderRepeat.DAILY, ReminderRepeat.CUSTOM_DAYS -> z.plusDays(n)
                ReminderRepeat.WEEKLY -> z.plusWeeks(n)
                ReminderRepeat.MONTHLY -> z.plusMonths(n)
                ReminderRepeat.YEARLY -> z.plusYears(n)
                else -> return null
            }.toInstant().toEpochMilli()
        }
    }
}
