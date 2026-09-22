package ir.kharjyar.app.widget

import android.content.Context
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
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.ContentScale
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.ui.theme.skinOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ویجت Glance: ساعت و تاریخ در سمت راست، خلاصه مالی در سمت چپ.
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

        val skin = skinOf(settings.palette)
        // میزان شیشه‌ای بودن از تنظیمات کاربر (۰ = کاملاً شفاف، ۱۰۰ = مات)
        val bg = ColorProvider(skin.cardColor.copy(alpha = settings.widgetOpacity / 100f))
        val accent = ColorProvider(skin.accent)
        val onBg = ColorProvider(skin.onBackdrop)
        val big = ColorProvider(skin.bigNumberColor)
        val showClock = settings.widgetShowClock
        val valueSize = settings.widgetValueSize.sp
        val labelSize = settings.widgetLabelSize.sp

        /**
         * ساعت به‌صورت RemoteViews با TextClock ساخته می‌شود، نه متن ثابت.
         * TextClock را خود سیستم‌عامل هر دقیقه به‌روز می‌کند، بنابراین ساعت ویجت
         * همیشه با ساعت گوشی سینک است و به بازه به‌روزرسانی ویجت وابسته نیست.
         */
        val clockViews = RemoteViews(context.packageName, R.layout.widget_clock).apply {
            val tz = PersianDate.TEHRAN.id

            // ساعت (زنده)
            setTextViewTextSize(R.id.widget_clock_text, TypedValue.COMPLEX_UNIT_SP, settings.widgetClockSize.toFloat())
            setTextColor(R.id.widget_clock_text, skin.bigNumberColor.toArgb())
            setCharSequence(R.id.widget_clock_text, "setFormat24Hour", "HH:mm")
            setString(R.id.widget_clock_text, "setTimeZone", tz)

            // تاریخ شمسی (متن، با تغییر روز بازنویسی می‌شود)
            setTextViewTextSize(R.id.widget_jalali_text, TypedValue.COMPLEX_UNIT_SP, settings.widgetDateSize.toFloat())
            setTextColor(R.id.widget_jalali_text, skin.onBackdrop.toArgb())
            setTextViewText(R.id.widget_jalali_text, persianDate)

            // تاریخ میلادی (زنده)
            setTextViewTextSize(
                R.id.widget_gregorian_text,
                TypedValue.COMPLEX_UNIT_SP,
                (settings.widgetDateSize * 0.85f)
            )
            setTextColor(R.id.widget_gregorian_text, skin.onBackdrop.copy(alpha = 0.85f).toArgb())
            setCharSequence(R.id.widget_gregorian_text, "setFormat24Hour", "d MMM yyyy")
            setCharSequence(R.id.widget_gregorian_text, "setFormat12Hour", "d MMM yyyy")
            setString(R.id.widget_gregorian_text, "setTimeZone", tz)

            // نمایش/پنهان‌سازی تاریخ‌ها طبق تنظیمات
            val dateVis = if (settings.widgetShowDates) android.view.View.VISIBLE else android.view.View.GONE
            setViewVisibility(R.id.widget_jalali_text, dateVis)
            setViewVisibility(R.id.widget_gregorian_text, dateVis)
        }

        // تصویر پس‌زمینه تم (مثل شکوفه شب) روی ویجت
        val bgImage = if (settings.widgetShowImage) skin.backdropImage else null

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(20.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    // لایه ۱: تصویر تم (اگر فعال باشد)
                    if (bgImage != null) {
                        Image(
                            provider = ImageProvider(bgImage),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = GlanceModifier.fillMaxSize().cornerRadius(20.dp)
                        )
                    }
                    // لایه ۲: رنگ نیمه‌شفاف — «میزان شیشه‌ای بودن» روی همین اعمال می‌شود
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(bg)
                            .cornerRadius(20.dp)
                    ) {}

                    // لایه ۳: محتوا
                    Row(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // ---------- سمت چپ: مقادیر مالی ----------
                    // (در RTL این ستون سمت چپ ویجت دیده می‌شود)
                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
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
                        // ---------- سمت راست: ساعت و تاریخ‌های زنده ----------
                        // همه در یک RemoteViews تا سیستم‌عامل خودش آن‌ها را به‌روز نگه دارد
                        Box(
                            modifier = GlanceModifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidRemoteViews(remoteViews = clockViews)
                        }
                    }
                    }
                }
            }
        }
    }
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
