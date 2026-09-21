package ir.kharjyar.app.notify

import android.Manifest
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.MainActivity

/**
 * اعلان‌های قابل‌اقدام. جزئیات مالی در متن اعلان نمی‌آید (حریم خصوصی صفحه قفل).
 * لمس اعلان صفحه مربوط به همان پیامک/تراکنش را باز می‌کند.
 */
object Notifier {

    const val EXTRA_DEST = "kharjyar_dest"
    const val EXTRA_SMS_ID = "kharjyar_sms_id"
    const val EXTRA_TX_ID = "kharjyar_tx_id"

    const val DEST_CONFIRM_TX = "confirm_tx"
    const val DEST_NEEDS_ACCOUNT = "needs_account"
    const val DEST_NEEDS_TEMPLATE = "needs_template"

    fun notifyDraftReady(context: Context, smsId: Long, txId: Long) {
        if (!canNotify(context)) return
        val pi = pendingIntent(context, smsId.toInt(), DEST_CONFIRM_TX, smsId, txId)
        val n = NotificationCompat.Builder(context, KharjYarApp.CHANNEL_TRANSACTIONS)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("تراکنش جدید شناسایی شد")
            .setContentText("برای تکمیل و تأیید «برای چه بود؟» لمس کنید")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(context, KharjYarApp.CHANNEL_TRANSACTIONS)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setContentTitle("خرج‌یار")
                    .setContentText("یک مورد جدید")
                    .build()
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        manager(context).notify(TAG_TX, smsId.toInt(), n)
    }

    fun notifyNeedsAccount(context: Context, smsId: Long) {
        if (!canNotify(context)) return
        val pi = pendingIntent(context, smsId.toInt(), DEST_NEEDS_ACCOUNT, smsId, null)
        val n = NotificationCompat.Builder(context, KharjYarApp.CHANNEL_REVIEW)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("پیامک بانکی از حساب ناشناخته")
            .setContentText("برای معرفی حساب جدید لمس کنید")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        manager(context).notify(TAG_REVIEW, smsId.toInt(), n)
    }

    fun notifyNeedsTemplate(context: Context, smsId: Long) {
        if (!canNotify(context)) return
        val pi = pendingIntent(context, smsId.toInt(), DEST_NEEDS_TEMPLATE, smsId, null)
        val n = NotificationCompat.Builder(context, KharjYarApp.CHANNEL_REVIEW)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("پیامک نیازمند بررسی")
            .setContentText("قالب این پیامک شناخته نشد؛ برای آموزش لمس کنید")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        manager(context).notify(TAG_REVIEW, smsId.toInt(), n)
    }

    private fun pendingIntent(context: Context, requestCode: Int, dest: String, smsId: Long?, txId: Long?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_DEST, dest)
            smsId?.let { putExtra(EXTRA_SMS_ID, it) }
            txId?.let { putExtra(EXTRA_TX_ID, it) }
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canNotify(context: Context): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            // در Android 12 مجوز POST_NOTIFICATIONS وجود ندارد
            androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    private fun manager(context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private const val TAG_TX = "tx"
    private const val TAG_REVIEW = "review"
}
