package ir.kharjyar.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.action.actionStartActivity
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
        val clock = Digits.toPersian(String.format(Locale.US, "%02d:%02d", zoned.hour, zoned.minute))
        val persianDate = "${today.dayOfWeekName()} ${Digits.toPersian(today.day.toString())} ${today.monthName()}"
        val gregorian = zoned.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))

        val skin = skinOf(settings.palette)
        val bg = ColorProvider(skin.cardColor.copy(alpha = if (skin.cardAlpha < 0.8f) 0.92f else skin.cardAlpha))
        val accent = ColorProvider(skin.accent)
        val onBg = ColorProvider(skin.onBackdrop)
        val big = ColorProvider(skin.bigNumberColor)
        val showClock = settings.widgetShowClock

        provideContent {
            GlanceTheme {
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bg)
                        .cornerRadius(20.dp)
                        .padding(14.dp)
                        .clickable(actionStartActivity<MainActivity>()),
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
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accent)
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        lines.forEach { (label, value) ->
                            Text(
                                label,
                                style = TextStyle(fontSize = 10.sp, color = onBg)
                            )
                            Text(
                                if (showNumbers) value else "••••",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showNumbers) big else onBg
                                )
                            )
                            Spacer(GlanceModifier.height(4.dp))
                        }
                        if (hideNumbers) {
                            Text(
                                "اعداد به دلیل قفل مخفی‌اند",
                                style = TextStyle(fontSize = 9.sp, color = onBg)
                            )
                        }
                    }

                    if (showClock) {
                        Spacer(GlanceModifier.width(12.dp))
                        // ---------- سمت راست: ساعت بزرگ + تاریخ شمسی + میلادی ----------
                        Column(
                            modifier = GlanceModifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                clock,
                                style = TextStyle(
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = big
                                )
                            )
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                persianDate,
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = onBg)
                            )
                            Text(
                                gregorian,
                                style = TextStyle(fontSize = 11.sp, color = onBg)
                            )
                        }
                    }
                }
            }
        }
    }
}

class KharjYarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KharjYarWidget()
}

object WidgetUpdater {
    /** به‌روزرسانی همه نمونه‌های ویجت پس از تغییر داده/تنظیمات. */
    fun requestUpdate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { KharjYarWidget().updateAll(context.applicationContext) }
        }
    }
}
