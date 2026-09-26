package ir.kharjyar.app

import ir.kharjyar.app.report.ExcelExporter
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class ExcelExporterTest {
    @Test fun createsRealXlsxWorkbookWithEscapedPersianContent() {
        val out = ByteArrayOutputStream()
        ExcelExporter.export(listOf(ExcelExporter.Row("۱۴۰۵/۰۱/۰۲", "روزمره", "برداشت", "خرید", "سوپرمارکت & خانه", "۱۰۰۰")), out)
        val entries = mutableMapOf<String, String>()
        ZipInputStream(out.toByteArray().inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        assertTrue(entries.keys.contains("xl/workbook.xml"))
        assertTrue(entries.getValue("xl/worksheets/sheet1.xml").contains("سوپرمارکت &amp; خانه"))
    }
}
