package ir.kharjyar.app.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.kharjyar.app.KharjYarApp

class ObligationReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as KharjYarApp
        val until = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
        val debts = app.database.debtDao().dueReminders(until)
        val checks = app.database.checkDao().dueReminders(until)
        if (debts.isEmpty() && checks.isEmpty()) return Result.success()
        if (android.os.Build.VERSION.SDK_INT < 33 || androidx.core.content.ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val text = listOfNotNull(debts.takeIf { it.isNotEmpty() }?.let { "${it.size} طلب/بدهی" }, checks.takeIf { it.isNotEmpty() }?.let { "${it.size} چک" }).joinToString(" و ")
            NotificationManagerCompat.from(applicationContext).notify(8801, NotificationCompat.Builder(applicationContext, KharjYarApp.CHANNEL_REVIEW).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("یادآور سررسید خرج‌یار").setContentText("$text نزدیک سررسید است").setAutoCancel(true).build())
        }
        return Result.success()
    }
}
