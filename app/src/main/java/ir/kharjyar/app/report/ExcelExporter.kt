package ir.kharjyar.app.report

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** خروجی XLSX استاندارد و سازگار با Excel، Google Sheets و LibreOffice. */
object ExcelExporter {
    data class Row(val date:String,val account:String,val type:String,val category:String,val description:String,val amount:String)

    fun export(rows:List<Row>,output:OutputStream){
        val all=listOf(Row("تاریخ و ساعت","حساب","نوع","دسته","شرح","مبلغ"))+rows
        val lastRow=all.size.coerceAtLeast(1)
        ZipOutputStream(output).use{zip->
            fun entry(name:String,text:String){zip.putNextEntry(ZipEntry(name));zip.write(text.toByteArray(Charsets.UTF_8));zip.closeEntry()}
            entry("[Content_Types].xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>""")
            entry("_rels/.rels","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            entry("xl/workbook.xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><bookViews><workbookView activeTab="0" firstSheet="0"/></bookViews><sheets><sheet name="گزارش خرج‌یار" sheetId="1" state="visible" r:id="rId1"/></sheets></workbook>""")
            entry("xl/_rels/workbook.xml.rels","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>""")
            entry("xl/styles.xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="2"><font><sz val="11"/><name val="Arial"/></font><font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Arial"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF2563EB"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="2"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="right" vertical="center" readingOrder="2" wrapText="1"/></xf><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center" readingOrder="2"/></xf></cellXfs></styleSheet>""")
            val sheetRows=all.mapIndexed{ri,row->val cells=listOf(row.date,row.account,row.type,row.category,row.description,row.amount).mapIndexed{ci,value->val col=('A'.code+ci).toChar();val style=if(ri==0)1 else 0;"<c r=\"$col${ri+1}\" s=\"$style\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xml(value)}</t></is></c>"}.joinToString("");"<row r=\"${ri+1}\"${if(ri==0)" ht=\"24\" customHeight=\"1\"" else ""}>$cells</row>"}.joinToString("")
            entry("xl/worksheets/sheet1.xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><dimension ref="A1:F$lastRow"/><sheetViews><sheetView rightToLeft="1" tabSelected="1" topLeftCell="A1" workbookViewId="0"><selection activeCell="A1" sqref="A1"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight="20"/><cols><col min="1" max="1" width="22" customWidth="1"/><col min="2" max="4" width="18" customWidth="1"/><col min="5" max="5" width="40" customWidth="1"/><col min="6" max="6" width="22" customWidth="1"/></cols><sheetData>$sheetRows</sheetData><autoFilter ref="A1:F$lastRow"/></worksheet>""")
        }
    }
    private fun xml(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;")
}
