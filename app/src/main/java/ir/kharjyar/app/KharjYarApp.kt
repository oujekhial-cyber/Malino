package ir.kharjyar.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import ir.kharjyar.app.data.Repository
import ir.kharjyar.app.data.db.KharjYarDatabase
import ir.kharjyar.app.data.prefs.SettingsRepository

class KharjYarApp : Application() {

    val database: KharjYarDatabase by lazy { KharjYarDatabase.get(this) }
    val repository: Repository by lazy { Repository(database) }
    val settings: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val txChannel = NotificationChannel(
            CHANNEL_TRANSACTIONS,
            "تراکنش‌های جدید",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان تراکنش‌های تشخیص‌داده‌شده از پیامک بانکی"
            // پیش‌فرض: مخفی بودن جزئیات مالی روی صفحه قفل
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }
        val reviewChannel = NotificationChannel(
            CHANNEL_REVIEW,
            "نیازمند بررسی",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "پیامک‌هایی که به معرفی حساب یا آموزش قالب نیاز دارند"
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }
        nm.createNotificationChannels(listOf(txChannel, reviewChannel))
    }

    companion object {
        const val CHANNEL_TRANSACTIONS = "transactions"
        const val CHANNEL_REVIEW = "review"
    }
}
