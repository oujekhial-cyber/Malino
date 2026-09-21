package ir.kharjyar.app.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import ir.kharjyar.app.R
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import java.io.OutputStream

/**
 * خروجی PDF فارسی و راست‌به‌چپ، کاملاً آفلاین.
 * از StaticLayout برای شکل‌دهی صحیح حروف فارسی و جهت RTL استفاده می‌شود
 * (drawText خام برای فارسی کافی نیست).
 */
object PdfExporter {

    data class ReportRow(
        val dateText: String,
        val accountTitle: String,
        val description: String,
        val categoryName: String,
        val natureText: String,
        val amountText: String
    )

    data class ReportData(
        val title: String,
        val rangeText: String,
        val filtersText: String,
        val incomeText: String,
        val expenseText: String,
        val netText: String,
        val pendingNote: String?,
        val incomeSeries: List<Long>,
        val expenseSeries: List<Long>,
        val rows: List<ReportRow>
    )

    private const val PAGE_W = 595 // A4 @72dpi
    private const val PAGE_H = 842
    private const val MARGIN = 40f

    fun export(context: Context, data: ReportData, out: OutputStream) {
        val typeface = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val boldTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        val doc = PdfDocument()
        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN

        fun newPage() {
            drawFooter(canvas, typeface, pageNumber)
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
            canvas = page.canvas
            y = MARGIN
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_H - MARGIN - 24) newPage()
        }

        // عنوان
        y += drawRtlText(canvas, data.title, boldTypeface, 20f, y, Color.BLACK)
        y += 4
        y += drawRtlText(canvas, data.rangeText, typeface, 12f, y, Color.DKGRAY)
        if (data.filtersText.isNotBlank()) {
            y += drawRtlText(canvas, data.filtersText, typeface, 11f, y, Color.DKGRAY)
        }
        y += 12

        // خلاصه
        ensureSpace(80f)
        val summaryPaint = Paint().apply { color = 0xFFEFF3F8.toInt() }
        canvas.drawRoundRect(MARGIN, y, PAGE_W - MARGIN, y + 70, 8f, 8f, summaryPaint)
        var sy = y + 8
        sy += drawRtlText(canvas, "درآمد: ${data.incomeText}", typeface, 12f, sy, Color.BLACK, inset = 12f)
        sy += drawRtlText(canvas, "هزینه: ${data.expenseText}", typeface, 12f, sy, Color.BLACK, inset = 12f)
        drawRtlText(canvas, "خالص: ${data.netText}", boldTypeface, 12f, sy, Color.BLACK, inset = 12f)
        y += 82
        data.pendingNote?.let {
            y += drawRtlText(canvas, it, typeface, 10f, y, Color.DKGRAY)
            y += 4
        }

        // نمودار خطی
        if (data.incomeSeries.size >= 2 || data.expenseSeries.size >= 2) {
            ensureSpace(150f)
            drawLineChart(canvas, data.incomeSeries, data.expenseSeries, y, 130f)
            y += 140
            y += drawRtlText(canvas, "— درآمد (آبی)   — هزینه (قرمز)", typeface, 10f, y, Color.DKGRAY)
            y += 8
        }

        // جدول تراکنش‌ها
        ensureSpace(40f)
        y += drawRtlText(canvas, "تراکنش‌ها (${Digits.toPersian(data.rows.size.toString())} مورد)", boldTypeface, 13f, y, Color.BLACK)
        y += 6

        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        for (row in data.rows) {
            ensureSpace(38f)
            val rowText = "${row.dateText} | ${row.accountTitle} | ${row.natureText} | ${row.amountText}" +
                if (row.description.isNotBlank() || row.categoryName.isNotBlank())
                    "\n${row.categoryName}${if (row.categoryName.isNotBlank() && row.description.isNotBlank()) " — " else ""}${row.description}"
                else ""
            val used = drawRtlText(canvas, rowText, typeface, 10f, y, Color.BLACK)
            y += used + 4
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, linePaint)
            y += 6
        }

        drawFooter(canvas, typeface, pageNumber)
        doc.finishPage(page)
        doc.writeTo(out)
        doc.close()
    }

    /** رسم متن RTL با StaticLayout؛ ارتفاع مصرف‌شده را برمی‌گرداند. */
    private fun drawRtlText(
        canvas: Canvas,
        text: String,
        typeface: Typeface,
        sizePt: Float,
        y: Float,
        color: Int,
        inset: Float = 0f
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = sizePt
            this.color = color
        }
        val width = (PAGE_W - 2 * MARGIN - 2 * inset).toInt()
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(android.text.TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        canvas.translate(MARGIN + inset, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun drawFooter(canvas: Canvas, typeface: Typeface, pageNumber: Int) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = 9f
            color = Color.GRAY
        }
        val text = "خرج‌یار — صفحه ${Digits.toPersian(pageNumber.toString())}"
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, PAGE_W - 2 * MARGIN.toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(android.text.TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        canvas.translate(MARGIN, PAGE_H - MARGIN + 6)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawLineChart(canvas: Canvas, income: List<Long>, expense: List<Long>, top: Float, height: Float) {
        val left = MARGIN
        val right = PAGE_W - MARGIN
        val w = right - left
        val maxValue = maxOf(income.maxOrNull() ?: 0L, expense.maxOrNull() ?: 0L, 1L)
        val n = maxOf(income.size, expense.size)
        if (n < 2) return
        val stepX = w / (n - 1)

        val gridPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }
        for (i in 0..3) {
            val gy = top + height * i / 3
            canvas.drawLine(left, gy, right, gy, gridPaint)
        }

        fun drawSeries(series: List<Long>, color: Int) {
            if (series.size < 2) return
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color; strokeWidth = 2f; style = Paint.Style.STROKE
            }
            val path = Path()
            series.forEachIndexed { i, v ->
                val x = right - i * stepX // RTL
                val yv = top + height - (v.toFloat() / maxValue) * height * 0.9f - height * 0.05f
                if (i == 0) path.moveTo(x, yv) else path.lineTo(x, yv)
            }
            canvas.drawPath(path, paint)
        }
        drawSeries(income, 0xFF1565C0.toInt())
        drawSeries(expense, 0xFFC62828.toInt())
    }
}
