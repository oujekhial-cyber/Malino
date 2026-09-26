package ir.kharjyar.app.report

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** خروجی واقعی XLSX بدون کتابخانه خارجی؛ فایل مستقیماً در Excel/LibreOffice باز می‌شود. */
object ExcelExporter {
    data class Row(val date: String, val account: String, val type: String, val category: String, val description: String, val amount: String)

    fun export(rows: List<Row>, output: OutputStream) {
        ZipOutputStream(output).use { zip ->
            fun entry(name: String, text: String) {
                zip.putNextEntry(ZipEntry(name)); zip.write(text.toByteArray(Charsets.UTF_8)); zip.closeEntry()
            }
            entry("[Content_Types].xml", """<?xml version="1.0"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""")
            entry("_rels/.rels", """<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            entry("xl/workbook.xml", """<?xml version="1.0"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="گزارش خرج‌یار" sheetId="1" r:id="rId1"/></sheets></workbook>""")
            entry("xl/_rels/workbook.xml.rels", """<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""")
            val all = listOf(Row("تاریخ و ساعت", "حساب", "نوع", "دسته", "شرح", "مبلغ")) + rows
            val sheetRows = all.mapIndexed { ri, row ->
                val cells = listOf(row.date,row.account,row.type,row.category,row.description,row.amount).mapIndexed { ci, value ->
                    val col = ('A'.code + ci).toChar(); "<c r=\"$col${ri+1}\" t=\"inlineStr\"><is><t>${xml(value)}</t></is></c>"
                }.joinToString("")
                "<row r=\"${ri+1}\">$cells</row>"
            }.joinToString("")
            entry("xl/worksheets/sheet1.xml", """<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetViews><sheetView rightToLeft="1" workbookViewId="0"/></sheetViews><sheetData>$sheetRows</sheetData></worksheet>""")
        }
    }
    private fun xml(s: String) = s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
}
