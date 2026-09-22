package ir.kharjyar.app.widget

import android.content.Context
import android.provider.AlarmClock
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.ContentScale
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityIntent
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.R
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.balance.AccountBalance
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.prefs.WidgetBackground
import ir.kharjyar.app.data.prefs.WidgetAlign
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.ui.theme.skinOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ویجت Glance: مقادیر مالی سمت راست، ساعت و تاریخ سمت چپ.
 * اگر قفل برنامه فعال است، اعداد به‌صورت پیش‌فرض مخفی‌اند مگر کاربر صریحاً اجازه دهد.
 */
class KharjYarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as KharjYarApp
        val settings = app.settings.current()
        val hideNumbers = settings.appLockEnabled && !settings.widgetShowNumbersWhenLocked
        val showNumbers = settings.widgetShowNumbers && !hideNumbers

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
            WidgetContent.TODAY_EXPENSE -> listOf("هزینه امروز" to Money.format(todaySummary.expenseRial, unit))
            WidgetContent.MONTH_EXPENSE -> listOf("هزینه ${today.monthName()}" to Money.format(monthSummary.expenseRial, unit))
            WidgetContent.SUMMARY -> listOfNotNull(
                balanceLine,
                "درآمد ${today.monthName()}" to Money.format(monthSummary.incomeRial, unit),
                "هزینه ${today.monthName()}" to Money.format(monthSummary.expenseRial, unit)
            )
            WidgetContent.RECENT -> recent.map { tx ->
                (if (tx.direction == TxDirection.DEPOSIT) "واریز" else "برداشت") to Money.format(tx.amountRial, unit)
            }.ifEmpty { listOf("تراکنش اخیر" to "—") }
        }

        // ---------- ساعت و تاریخ‌ها ----------
        val nowMillis = System.currentTimeMillis()
        val zoned = Instant.ofEpochMilli(nowMillis).atZone(PersianDate.TEHRAN)
        val persianDate = "${today.dayOfWeekName()} ${Digits.toPersian(today.day.toString())} ${today.monthName()}"
        val gregorian = zoned.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))

        val skin = skinOf(settings.palette)
        // میزان شیشه‌ای بودن از تنظیمات کاربر (۰ = کاملاً شفاف، ۱۰۰ = مات)
        val accent = ColorProvider(skin.accent)
        val onBg = ColorProvider(skin.onBackdrop)
        val big = ColorProvider(skin.bigNumberColor)
        val showClock = settings.widgetShowClock
        val useSakuraBg = settings.widgetShowImage &&
            settings.widgetBackground == WidgetBackground.SAKURA
        val dateSize = settings.widgetDateSize.sp
        val valueSize = settings.widgetValueSize.sp
        val labelSize = settings.widgetLabelSize.sp

        // تراز افقی انتخابی کاربر برای دو پنل ویجت
        fun horizOf(a: WidgetAlign) = when (a) {
            WidgetAlign.START -> Alignment.Start
            WidgetAlign.CENTER -> Alignment.CenterHorizontally
            WidgetAlign.END -> Alignment.End
        }
        // جابه‌جایی عمودی با padding نامتقارن شبیه‌سازی می‌شود
        val titleOff = settings.widgetTitleOffsetY
        val clockOff = settings.widgetClockOffsetY
        val titleShiftTop = (if (titleOff > 0) titleOff else 0).dp
        val titleShiftBottom = (if (titleOff < 0) -titleOff else 0).dp
        val clockShiftTop = (if (clockOff > 0) clockOff else 0).dp
        val clockShiftBottom = (if (clockOff < 0) -clockOff else 0).dp
        val titleAlign = horizOf(settings.widgetTitleAlign)
        val clockAlign = horizOf(settings.widgetClockAlign)
        // gravity متناظر برای چیدمان XML پنل ساعت
        val clockGravity = when (settings.widgetClockAlign) {
            WidgetAlign.START -> android.view.Gravity.START
            WidgetAlign.CENTER -> android.view.Gravity.CENTER_HORIZONTAL
            WidgetAlign.END -> android.view.Gravity.END
        }

        /**
         * ساعت به‌صورت RemoteViews با TextClock ساخته می‌شود، نه متن ثابت.
         * TextClock را خود سیستم‌عامل هر دقیقه به‌روز می‌کند، بنابراین ساعت ویجت
         * همیشه با ساعت گوشی سینک است و به بازه به‌روزرسانی ویجت وابسته نیست.
         */
        val clockViews = RemoteViews(context.packageName, R.layout.widget_clock).apply {
            val clockPx = settings.widgetClockSize.toFloat()
            val datePx = settings.widgetDateSize.toFloat()
            val bigArgb = skin.bigNumberColor.toArgb()
            val onArgb = skin.onBackdrop.toArgb()
            val tz = PersianDate.TEHRAN.id

            // ساعت و دقیقه، هر دو زنده و هم‌اندازه
            setTextViewTextSize(R.id.widget_hour, TypedValue.COMPLEX_UNIT_SP, clockPx)
            setTextViewTextSize(R.id.widget_minute, TypedValue.COMPLEX_UNIT_SP, clockPx)
            setTextColor(R.id.widget_hour, bigArgb)
            setTextColor(R.id.widget_minute, bigArgb)
            setString(R.id.widget_hour, "setTimeZone", tz)
            setString(R.id.widget_minute, "setTimeZone", tz)

            // تاریخ شمسی (از کد) و تاریخ میلادی (زنده) — قابل خاموش کردن
            val datesVisibility = if (settings.widgetShowDates) android.view.View.VISIBLE else android.view.View.GONE
            setViewVisibility(R.id.widget_jalali, datesVisibility)
            setViewVisibility(R.id.widget_gregorian, datesVisibility)
            setTextViewText(R.id.widget_jalali, persianDate)
            setTextViewTextSize(R.id.widget_jalali, TypedValue.COMPLEX_UNIT_SP, datePx)
            setTextColor(R.id.widget_jalali, onArgb)

            setTextViewTextSize(R.id.widget_gregorian, TypedValue.COMPLEX_UNIT_SP, datePx * 0.85f)
            setTextColor(R.id.widget_gregorian, onArgb)
            setString(R.id.widget_gregorian, "setTimeZone", tz)

            // تراز افقی متن‌های پنل ساعت
            setInt(R.id.widget_clock_panel, "setGravity", clockGravity)
            setInt(R.id.widget_hour, "setGravity", clockGravity)
            setInt(R.id.widget_minute, "setGravity", clockGravity)
            setInt(R.id.widget_jalali, "setGravity", clockGravity)
            setInt(R.id.widget_gregorian, "setGravity", clockGravity)
        }

        /**
         * پس‌زمینه به‌صورت یک بیت‌مپ آماده ساخته می‌شود (نه لایه RemoteViews جداگانه).
         * دلیل: تودرتو کردن چند AndroidRemoteViews داخل Glance باعث خطای
         * «can't load widget» در بعضی لانچرها می‌شد. با کشیدن تصویر و رنگ روی یک
         * بوم واحد، هم آن خطا برطرف می‌شود و هم شفافیت واقعاً اعمال می‌گردد.
         */
        val alphaFraction = (settings.widgetOpacity / 100f).coerceIn(0f, 1f)
        val bgBitmap: Bitmap? = runCatching {
            if (!useSakuraBg) return@runCatching null
            val src = BitmapFactory.decodeResource(context.resources, R.drawable.widget_bg_sakura)
                ?: return@runCatching null
            val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            // تصویر با شفافیت انتخابی کاربر
            canvas.drawBitmap(src, 0f, 0f, Paint().apply {
                alpha = (alphaFraction * 255).toInt().coerceIn(0, 255)
                isFilterBitmap = true
            })
            // لایه رنگ تم روی تصویر تا لحن تم حفظ شود
            canvas.drawColor(skin.cardColor.copy(alpha = alphaFraction * 0.5f).toArgb())
            if (src != out) src.recycle()
            out
        }.getOrNull()

        // اگر تصویری در کار نیست، فقط رنگ تم با شفازیت انتخابی روی پس‌زمینه می‌نشیند
        val plainBg = ColorProvider(skin.cardColor.copy(alpha = alphaFraction))

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(20.dp)
                ) {
                    // لایه پس‌زمینه: یا بیت‌مپ آماده (تصویر + رنگ) یا فقط رنگ تم
                    if (bgBitmap != null) {
                        Image(
                            provider = ImageProvider(bgBitmap),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(20.dp)
                        )
                    } else {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .background(plainBg)
                                .cornerRadius(20.dp)
                        ) {}
                    }

                    Row(
                        modifier = GlanceModifier.fillMaxSize().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // ---------- سمت راست (در RTL اول می‌آید): مقادیر مالی ----------
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            // لمس این ناحیه، خودِ خرج‌یار را باز می‌کند
                            .clickable(actionStartActivity<MainActivity>())
                            .padding(
                                top = titleShiftTop,
                                bottom = titleShiftBottom
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = titleAlign
                    ) {
                        Text(
                            "خرج‌یار",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = labelSize * 1.3f, color = accent)
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        lines.forEach { (label, value) ->
                            Text(
                                label,
                                style = TextStyle(fontSize = labelSize, color = onBg)
                            )
                            Text(
                                if (showNumbers) value else "••••",
                                style = TextStyle(
                                    fontSize = valueSize,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showNumbers) big else onBg
                                )
                            )
                            Spacer(GlanceModifier.height(4.dp))
                        }
                        if (hideNumbers) {
                            Text(
                                "اعداد به دلیل قفل مخفی‌اند",
                                style = TextStyle(fontSize = labelSize * 0.9f, color = onBg)
                            )
                        }
                    }

                    if (showClock) {
                        Spacer(GlanceModifier.width(12.dp))
                        // ---------- سمت چپ: ساعت بزرگ + تاریخ شمسی + میلادی ----------
                        Column(
                            modifier = GlanceModifier
                                .fillMaxHeight()
                                // لمس ساعت، برنامه ساعت گوشی را باز می‌کند
                                .clickable(actionStartActivityIntent(clockIntent(context)))
                                .padding(
                                    top = clockShiftTop,
                                    bottom = clockShiftBottom
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = clockAlign
                        ) {
                            // ساعت/دقیقه/تاریخ‌ها همگی زنده‌اند و توسط سیستم به‌روز می‌شوند
                            AndroidRemoteViews(remoteViews = clockViews)
                        }
                    }
                    }
                }
            }
        }
    }
}

/**
 * اینتنت باز کردن برنامه ساعت گوشی.
 * اول اکشن استاندارد «نمایش ساعت‌ها» امتحان می‌شود؛ اگر روی دستگاه پشتیبانی
 * نشود، سراغ زنگ هشدار و در نهایت برنامه پیش‌فرض ساعت می‌رویم.
 */
private fun clockIntent(context: Context): Intent {
    val pm = context.packageManager
    val candidates = listOf(
        Intent(AlarmClock.ACTION_SHOW_ALARMS),
        Intent(AlarmClock.ACTION_SET_ALARM)
    )
    candidates.forEach { intent ->
        // resolveActivityInfo روی همه نسخه‌ها در دسترس است و null-safe بررسی می‌شود
        if (intent.resolveActivityInfo(pm, 0) != null) {
            return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    // تلاش نهایی: برنامه ساعت رایج روی اکثر دستگاه‌ها
    val fallback = pm.getLaunchIntentForPackage("com.android.deskclock")
        ?: pm.getLaunchIntentForPackage("com.google.android.deskclock")
    return (fallback ?: Intent(context, MainActivity::class.java))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

class KharjYarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KharjYarWidget()

    /**
     * ساعت خودش با TextClock زنده است، ولی تاریخ شمسی/میلادی متن ثابت است.
     * با گوش دادن به تغییر روز/ساعت/منطقه زمانی، ویجت سر نیمه‌شب بازسازی می‌شود.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> WidgetUpdater.requestUpdate(context)
        }
    }
}

object WidgetUpdater {
    /** به‌روزرسانی همه نمونه‌های ویجت پس از تغییر داده/تنظیمات. */
    fun requestUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { KharjYarWidget().updateAll(context.applicationContext) }
        }
    }
}
