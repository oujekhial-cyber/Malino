package ir.kharjyar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.ui.theme.LocalAppSkin

/** متن مبلغ با واحد نمایش. اعداد به‌صورت LTR داخل متن RTL درست نمایش داده می‌شوند. */
@Composable
fun MoneyText(
    rial: Long,
    unit: MoneyUnit,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.Unspecified
) {
    Text(
        text = Money.format(rial, unit),
        modifier = modifier,
        style = style,
        color = color,
        textAlign = TextAlign.Start
    )
}

/**
 * نشانگر نوع تراکنش: آیکون + متن (رنگ تنها راه تشخیص نیست).
 */
@Composable
fun DirectionBadge(direction: Int, nature: Int, modifier: Modifier = Modifier) {
    val (icon, label, color) = when {
        nature == TxNature.TRANSFER -> Triple(Icons.Filled.SwapHoriz, "انتقال", MaterialTheme.colorScheme.tertiary)
        direction == TxDirection.DEPOSIT -> Triple(Icons.AutoMirrored.Filled.TrendingUp, "واریز", LocalAppSkin.current.incomeColor)
        else -> Triple(Icons.AutoMirrored.Filled.TrendingDown, "برداشت", LocalAppSkin.current.expenseColor)
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** حالت خالی طراحی‌شده. */
@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("۰", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * نمودار خطی ساده و تم‌پذیر: دو سری (درآمد/هزینه) روی محور زمان.
 */
@Composable
fun LineChart(
    incomeSeries: List<Long>,
    expenseSeries: List<Long>,
    modifier: Modifier = Modifier,
    incomeColor: Color = LocalAppSkin.current.incomeColor,
    expenseColor: Color = LocalAppSkin.current.expenseColor
) {
    val skin = LocalAppSkin.current
    val gridColor = skin.onBackdrop.copy(alpha = 0.12f)
    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val n = maxOf(incomeSeries.size, expenseSeries.size)
        if (n < 2) return@Canvas
        val maxValue = maxOf(incomeSeries.maxOrNull() ?: 0L, expenseSeries.maxOrNull() ?: 0L, 1L)
        val w = size.width
        val h = size.height
        val stepX = w / (n - 1)

        // خطوط شبکه افقی
        for (i in 0..3) {
            val y = h * i / 3f
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        fun pathOf(series: List<Long>): Path? {
            if (series.size < 2) return null
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = w - i * stepX // RTL: زمان از راست به چپ
                val y = h - (v.toFloat() / maxValue) * (h * 0.92f) - h * 0.04f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
        }

        fun drawSeries(series: List<Long>, color: Color) {
            val path = pathOf(series) ?: return
            if (skin.chartGlow) {
                // افکت درخشش: چند لایه ضخیم و کم‌رنگ زیر خط اصلی
                drawPath(path, color.copy(alpha = 0.10f), style = Stroke(width = 18f, cap = StrokeCap.Round))
                drawPath(path, color.copy(alpha = 0.18f), style = Stroke(width = 11f, cap = StrokeCap.Round))
            }
            drawPath(path, color, style = Stroke(width = 5f, cap = StrokeCap.Round))
        }
        drawSeries(incomeSeries, incomeColor)
        drawSeries(expenseSeries, expenseColor)
    }
}
