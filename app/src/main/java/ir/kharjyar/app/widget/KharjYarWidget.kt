package ir.kharjyar.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.MainActivity
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.data.prefs.WidgetContent
import ir.kharjyar.app.ui.theme.skinOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ویجت Glance: خلاصه هزینه/درآمد روی صفحه اصلی.
 * اگر قفل برنامه فعال است، اعداد به‌صورت پیش‌فرض مخفی‌اند مگر کاربر صریحاً اجازه دهد.
 */
class KharjYarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as KharjYarApp
        val settings = app.settings.current()
        val hideNumbers = settings.appLockEnabled && !settings.widgetShowNumbersWhenLocked
        val showNumbers = settings.widgetShowNumbers && !hideNumbers

        // داده‌ها را قبل از provideContent می‌خوانیم
        val today = PersianDate.today()
        val (monthFrom, monthTo) = app.repository.currentPersianMonthRange()
        val monthSummary = app.repository.summary(monthFrom, monthTo)
        val todaySummary = app.repository.summary(today.startOfDayMillis(), today.endOfDayMillisExclusive())
        val recent = if (settings.widgetContent == WidgetContent.RECENT) {
            app.repository.txDao.listRange(monthFrom, monthTo).takeLast(3).reversed()
        } else emptyList()

        val unit = settings.moneyUnit
        val lines: List<Pair<String, String>> = when (settings.widgetContent) {
            WidgetContent.TODAY_EXPENSE -> listOf("هزینه امروز" to Money.format(todaySummary.expenseRial, unit))
            WidgetContent.MONTH_EXPENSE -> listOf("هزینه ${today.monthName()}" to Money.format(monthSummary.expenseRial, unit))
            WidgetContent.SUMMARY -> listOf(
                "درآمد ${today.monthName()}" to Money.format(monthSummary.incomeRial, unit),
                "هزینه ${today.monthName()}" to Money.format(monthSummary.expenseRial, unit)
            )
            WidgetContent.RECENT -> recent.map { tx ->
                (if (tx.direction == 0) "واریز" else "برداشت") to Money.format(tx.amountRial, unit)
            }.ifEmpty { listOf("تراکنش اخیر" to "—") }
        }

        val skin = skinOf(settings.palette)
        val bg = ColorProvider(skin.cardColor.copy(alpha = if (skin.cardAlpha < 0.8f) 0.9f else skin.cardAlpha))
        val accent = ColorProvider(skin.accent)
        val onBg = ColorProvider(skin.onBackdrop)
        val big = ColorProvider(skin.bigNumberColor)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bg)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "خرج‌یار",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accent
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    lines.forEach { (label, value) ->
                        Text(
                            "$label: " + if (showNumbers) value else "••••",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showNumbers) big else onBg
                            )
                        )
                    }
                    if (hideNumbers) {
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            "اعداد به دلیل قفل برنامه مخفی‌اند",
                            style = TextStyle(fontSize = 10.sp, color = onBg)
                        )
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
