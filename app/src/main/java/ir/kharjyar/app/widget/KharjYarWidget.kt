package ir.kharjyar.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.R
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.data.prefs.WidgetLayout
import ir.kharjyar.app.ui.theme.AppSkin
import ir.kharjyar.app.ui.theme.skinOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ویجت خرج‌یار.
 *
 * با RemoteViews خالص ساخته می‌شود (نه Glance). دلیل: در Glance کلیک روی اجزای
 * داخلی به بیرون نشت می‌کرد و لمس ساعت هم برنامه را باز می‌کرد. اینجا هر ناحیه
 * PendingIntent مستقل دارد: ناحیه ساعت → برنامه ساعت گوشی، بقیه → خرج‌یار.
 *
 * ساعت و تاریخ میلادی TextClock هستند، پس سیستم‌عامل خودش زنده نگهشان می‌دارد.
 */
object WidgetRenderer {

    /**
     * نمای اضطراری: اگر ساخت نمای کامل شکست بخورد (خواندن تنظیمات، دیتابیس یا بیت‌مپ)،
     * این نسخه ساده نمایش داده می‌شود تا لانچر خطای «can't load widget» ندهد.
     * فقط ساعت و تاریخ زنده را نشان می‌دهد و با لمس، برنامه باز می‌شود.
     */
    fun buildFallback(context: Context): RemoteViews =
        RemoteViews(context.packageName, R.layout.w_split).apply {
            val tz = PersianDate.TEHRAN.id
            setString(R.id.w_clock, "setTimeZone", tz)
            setString(R.id.w_gregorian, "setTimeZone", tz)
            setTextViewText(R.id.w_title, "خرج‌یار")
            setTextViewText(R.id.w_jalali, runCatching { PersianDate.today().format() }.getOrDefault(""))
            // ردیف‌های مقدار در حالت اضطراری خالی می‌مانند
            setViewVisibility(R.id.w_row_2, android.view.View.GONE)
            setViewVisibility(R.id.w_row_3, android.view.View.GONE)
            setTextViewText(R.id.w_label_1, "")
            setTextViewText(R.id.w_value_1, "")
            applyClickTargets(context, this)
        }

    /** ساخت نمای کامل ویجت بر اساس تنظیمات کاربر. */
    suspend fun build(context: Context): RemoteViews {
        val app = context.applicationContext as KharjYarApp
        val settings = app.settings.current()
        val skin = skinOf(settings.palette)

        val hideNumbers = settings.appLockEnabled && !settings.widgetShowNumbersWhenLocked
        val showNumbers = settings.widgetShowNumbers && !hideNumbers

        // ---------- داده‌ها ----------
        val today = PersianDate.today()
        val (monthFrom, monthTo) = app.repository.currentPersianMonthRange()
        val accountId = settings.defaultAccountId
        val monthSummary = app.repository.summary(monthFrom, monthTo, accountId)
        val todaySummary = app.repository.summary(
            today.startOfDayMillis(), today.endOfDayMillisExclusive(), accountId
        )
        val recent = if (settings.widgetContent == WidgetContent.RECENT) {
            app.repository.txDao.listRange(monthFrom, monthTo)
                .filter { accountId == null || it.accountId == accountId }
                .takeLast(3).reversed()
        } else emptyList()

        val defaultAccount = accountId?.let { app.repository.accountDao.byId(it) }
        val balanceLine = defaultAccount?.let { acc ->
            val est = AccountBalance.estimate(acc, app.repository.txDao.allOnce())
            est.rial?.let { "مانده ${acc.title}" to Money.format(it, settings.moneyUnit) }
        }

        val unit = settings.moneyUnit
        val lines: List<Pair<String, String>> = when (settings.widgetContent) {
            WidgetContent.TODAY_EXPENSE ->
                listOf("هزینه امروز" to Money.format(todaySummary.expenseRial, unit))
            WidgetContent.MONTH_EXPENSE ->
                listOf("هزینه ${today.monthName()}" to Money.format(monthSummary.expenseRial, unit))
            WidgetContent.SUMMARY -> listOfNotNull(
                "درآمد ${today.monthName()}" to Money.format(monthSummary.incomeRial, unit),
                "هزینه ${today.monthName()}" to Money.format(monthSummary.expenseRial, unit),
                balanceLine
            )
            WidgetContent.RECENT -> recent.map { tx ->
                (if (tx.direction == TxDirection.DEPOSIT) "واریز" else "برداشت") to
                    Money.format(tx.amountRial, unit)
            }.ifEmpty { listOf("تراکنش اخیر" to "—") }
        }

        val persianDate =
            "${today.dayOfWeekName()} ${Digits.toPersian(today.day.toString())} ${today.monthName()}"

        // ---------- ساخت نما ----------
        val layoutRes = when (settings.widgetLayout) {
            WidgetLayout.ROYAL -> R.layout.w_royal
            WidgetLayout.MINIMAL -> R.layout.w_minimal
            WidgetLayout.PANELS, WidgetLayout.STACKED -> R.layout.w_panels
            WidgetLayout.SPLIT, WidgetLayout.GLASS -> R.layout.w_split
        }
        val views = RemoteViews(context.packageName, layoutRes)

        applyBackground(context, views, skin, settings.widgetOpacity, settings.widgetLayout)
        applyColors(views, skin, settings.widgetLayout)
        applyTexts(views, lines, persianDate, showNumbers, hideNumbers)
        applyClickTargets(context, views)

        return views
    }

    /**
     * پس‌زمینه: یک بیت‌مپ گرد با رنگ تم و شفافیت انتخابی کاربر.
     * چون بیت‌مپ خودش شفاف است، تصویر زمینه گوشی از پشتش دیده می‌شود.
     */
    private fun applyBackground(
        context: Context,
        views: RemoteViews,
        skin: AppSkin,
        opacity: Int,
        layout: WidgetLayout
    ) {
        val alpha = (opacity / 100f).coerceIn(0f, 1f)
        val bitmap = runCatching {
            // ابعاد عمداً کوچک است: RemoteViews سقف حجم دارد (حدود ۱.۵ تا ۲ مگابایت)
            // و بیت‌مپ بزرگ باعث خطای «can't load widget» در لانچر می‌شد.
            // ۴۸۰×۲۳۰ حدود ۰.۴ مگابایت است و چون فقط پس‌زمینهٔ نرم است، کشیده‌شدنش دیده نمی‌شود.
            val w = 480
            val h = 230
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val radius = 26f
            val rect = RectF(2f, 2f, w - 2f, h - 2f)

            // تصویر تم (فقط در قالب‌های شیشه‌ای) زیر رنگ کشیده می‌شود
            if (layout == WidgetLayout.GLASS || layout == WidgetLayout.ROYAL) {
                // نمونه‌برداری کاهشی تا تصویر منبع هم حافظه زیادی نگیرد
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                BitmapFactory.decodeResource(context.resources, R.drawable.widget_bg_sakura, opts)
                    ?.let { src ->
                        val saved = canvas.save()
                        canvas.clipRect(rect)
                        canvas.drawBitmap(
                            src,
                            null,
                            RectF(0f, 0f, w.toFloat(), h.toFloat()),
                            Paint().apply {
                                this.alpha = (alpha * 210).toInt().coerceIn(0, 255)
                                isFilterBitmap = true
                            }
                        )
                        canvas.restoreToCount(saved)
                        src.recycle()
                    }
            }

            // بدنه کارت با رنگ تم
            canvas.drawRoundRect(
                rect, radius, radius,
                Paint().apply {
                    isAntiAlias = true
                    color = skin.cardColor.copy(alpha = alpha).toArgb()
                }
            )
            // قاب نازک نورانی به رنگ تم
            canvas.drawRoundRect(
                rect, radius, radius,
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = skin.accent.copy(alpha = 0.45f * alpha + 0.12f).toArgb()
                }
            )
            out
        }.getOrNull()

        if (bitmap != null) {
            views.setImageViewBitmap(R.id.w_bg_img, bitmap)
        }
    }

    /** رنگ‌آمیزی متن‌ها و آیکون‌ها با رنگ‌های تم فعال. */
    private fun applyColors(views: RemoteViews, skin: AppSkin, layout: WidgetLayout) {
        val accent = skin.accent.toArgb()
        val onBg = skin.onBackdrop.toArgb()
        val big = skin.bigNumberColor.toArgb()
        val muted = skin.onBackdrop.copy(alpha = 0.72f).toArgb()

        views.setTextColor(R.id.w_title, accent)
        views.setTextColor(R.id.w_clock, big)
        views.setTextColor(R.id.w_jalali, onBg)
        views.setTextColor(R.id.w_gregorian, muted)

        listOf(R.id.w_label_1, R.id.w_label_2, R.id.w_label_3).forEach {
            views.setTextColor(it, onBg)
        }
        listOf(R.id.w_value_1, R.id.w_value_2, R.id.w_value_3).forEach {
            views.setTextColor(it, big)
        }

        // آیکون درآمد سبز/تم و هزینه قرمز/تم
        views.setInt(R.id.w_icon_1, "setColorFilter", skin.incomeColor.toArgb())
        views.setInt(R.id.w_icon_2, "setColorFilter", skin.expenseColor.toArgb())

        // منطقه زمانی ساعت‌ها روی تهران تنظیم می‌شود
        val tz = PersianDate.TEHRAN.id
        views.setString(R.id.w_clock, "setTimeZone", tz)
        views.setString(R.id.w_gregorian, "setTimeZone", tz)
    }

    /** پر کردن متن‌ها و پنهان/آشکار کردن ردیف‌های اضافه. */
    private fun applyTexts(
        views: RemoteViews,
        lines: List<Pair<String, String>>,
        persianDate: String,
        showNumbers: Boolean,
        hideNumbers: Boolean
    ) {
        views.setTextViewText(R.id.w_jalali, persianDate)

        val labelIds = listOf(R.id.w_label_1, R.id.w_label_2, R.id.w_label_3)
        val valueIds = listOf(R.id.w_value_1, R.id.w_value_2, R.id.w_value_3)
        val rowIds = listOf(null, R.id.w_row_2, R.id.w_row_3)

        labelIds.indices.forEach { i ->
            val line = lines.getOrNull(i)
            if (line == null) {
                rowIds[i]?.let { views.setViewVisibility(it, android.view.View.GONE) }
                views.setTextViewText(labelIds[i], "")
                views.setTextViewText(valueIds[i], "")
            } else {
                rowIds[i]?.let { views.setViewVisibility(it, android.view.View.VISIBLE) }
                views.setTextViewText(labelIds[i], line.first)
                views.setTextViewText(
                    valueIds[i],
                    if (showNumbers) line.second else "••••"
                )
            }
        }

        if (hideNumbers) {
            views.setTextViewText(R.id.w_label_1, "اعداد به دلیل قفل مخفی‌اند")
        }
    }

    /**
     * ناحیه‌های کلیک‌پذیر.
     * چون هر ناحیه PendingIntent جداگانه دارد، لمس ساعت واقعاً برنامه ساعت را
     * باز می‌کند و با لمس بقیه ویجت، خرج‌یار باز می‌شود.
     */
    private fun applyClickTargets(context: Context, views: RemoteViews) {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val appPending = PendingIntent.getActivity(
            context,
            REQ_APP,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            flags
        )
        // هر دو ناحیه خرج‌یار را باز می‌کنند. باز کردن برنامه ساعت گوشی روی
        // بعضی دستگاه‌ها کار نمی‌کرد، پس طبق خواست کاربر برداشته شد.
        views.setOnClickPendingIntent(R.id.w_app_area, appPending)
        views.setOnClickPendingIntent(R.id.w_clock_area, appPending)
    }

    private const val REQ_APP = 1001
}

/** گیرنده ویجت: رسم اولیه و به‌روزرسانی با تغییر روز/ساعت. */
class KharjYarWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        render(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        render(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    /**
     * ساعت خودش با TextClock زنده است، ولی تاریخ شمسی متن ثابت است؛
     * با تغییر روز/ساعت/منطقه زمانی ویجت دوباره رسم می‌شود.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> WidgetUpdater.requestUpdate(context)
        }
    }

    private fun render(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {
        if (ids.isEmpty()) return
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val views = runCatching { WidgetRenderer.build(appContext) }
                .getOrElse {
                    // اگر رسم کامل به هر دلیلی شکست بخورد، نمای ساده جایگزین می‌شود
                    // تا لانچر «can't load widget» نشان ندهد.
                    WidgetRenderer.buildFallback(appContext)
                }
            runCatching { ids.forEach { manager.updateAppWidget(it, views) } }
        }
    }
}

object WidgetUpdater {
    /** به‌روزرسانی همه نمونه‌های ویجت پس از تغییر داده یا تنظیمات. */
    fun requestUpdate(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val manager = AppWidgetManager.getInstance(appContext)
                val ids = manager.getAppWidgetIds(
                    ComponentName(appContext, KharjYarWidgetReceiver::class.java)
                )
                if (ids.isNotEmpty()) {
                    val views = runCatching { WidgetRenderer.build(appContext) }
                        .getOrElse { WidgetRenderer.buildFallback(appContext) }
                    ids.forEach { manager.updateAppWidget(it, views) }
                }
            }
        }
    }
}
