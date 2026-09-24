package ir.kharjyar.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import ir.kharjyar.app.data.Repository
import ir.kharjyar.app.data.db.KharjYarDatabase
import ir.kharjyar.app.data.prefs.SettingsRepository
import ir.kharjyar.app.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class KharjYarApp : Application() {

    val database: KharjYarDatabase by lazy { KharjYarDatabase.get(this) }
    val repository: Repository by lazy { Repository(database) }
    val settings: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        observeDataForWidget()
    }

    /**
     * ویجت را به جریان داده وصل می‌کند.
     *
     * قبلاً هر صفحه‌ای که داده را تغییر می‌داد باید خودش WidgetUpdater را صدا می‌زد؛
     * هر جا این کار فراموش می‌شد ویجت کهنه می‌ماند (مثلاً بعد از تأیید تراکنش در
     * صفحه بررسی). حالا مستقیماً به Room گوش می‌دهیم، پس هر تغییری — از هر مسیری —
     * خودبه‌خود ویجت را تازه می‌کند.
     *
     * تغییرات پشت‌سرهم با debounce جمع می‌شوند تا هنگام ثبت گروهی، ویجت ده‌ها بار
     * پشت هم بازسازی نشود.
     */
    @OptIn(FlowPreview::class)
    private fun observeDataForWidget() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        combine(
            repository.txDao.observeAll(),
            repository.accountDao.observeAll(),
            settings.settings
        ) { txs, accounts, _ -> txs.size to accounts.size }
            .debounce(400)
            // اولین مقدار هنگام راه‌اندازی رد می‌شود؛ ویجت همان موقع خودش رسم شده است
            .drop(1)
            .onEach { WidgetUpdater.requestUpdate(this@KharjYarApp) }
            .launchIn(scope)
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
