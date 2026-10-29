package ir.kharjyar.app

import ir.kharjyar.app.report.ExcelExporter
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class ExcelExporterTest {
    @Test fun createsRealXlsxWorkbookWithHeaderSummaryAccountNumberAndPersianContent() {
        val out = ByteArrayOutputStream()
        ExcelExporter.export(
            ExcelExporter.ReportData(
                title = "خرج‌یار — گزارش مالی",
                rangeText = "از تاریخ ۱۴۰۵/۰۱/۰۱ تا تاریخ ۱۴۰۵/۰۱/۳۱",
                incomeText = "۲٬۰۰۰ تومان",
                expenseText = "۱٬۰۰۰ تومان",
                netText = "۱٬۰۰۰ تومان",
                rows = listOf(ExcelExporter.Row("۱۴۰۵/۰۱/۰۲", "روزمره", "برداشت", "خرید", "سوپرمارکت & خانه", "۱۰۰۰", "۱۲۳۴۵۶۷۸۹"))
            ), out
        )
        val entries = mutableMapOf<String, String>()
        ZipInputStream(out.toByteArray().inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        assertTrue(entries.keys.contains("xl/workbook.xml"))
        assertTrue(entries.keys.contains("xl/styles.xml"))
        val sheet = entries.getValue("xl/worksheets/sheet1.xml")
        listOf("خرج‌یار — گزارش مالی","از تاریخ ۱۴۰۵/۰۱/۰۱ تا تاریخ ۱۴۰۵/۰۱/۳۱","جمع واریز","جمع برداشت","شماره حساب","۱۲۳۴۵۶۷۸۹","سوپرمارکت &amp; خانه").forEach { assertTrue(it, sheet.contains(it)) }
        assertTrue(sheet.contains("dimension ref=\"A1:H7\""))
        assertTrue(sheet.contains("mergeCell ref=\"A1:H1\""))
        assertTrue(sheet.contains("autoFilter ref=\"A6:H7\""))
        assertTrue(sheet.contains("<c r=\"A6\"") && sheet.contains("ردیف"))
        assertTrue(sheet.contains("<c r=\"A7\"") && sheet.contains("<v>1</v>"))
        assertTrue(sheet.contains("<c r=\"H7\""))
    }
}
