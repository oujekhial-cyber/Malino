package ir.kharjyar.app.report

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** خروجی XLSX استاندارد، راست‌به‌چپ و سازگار با Excel، Google Sheets و LibreOffice. */
object ExcelExporter {
    data class Row(
        val date:String,
        val account:String,
        val type:String,
        val category:String,
        val description:String,
        val amount:String,
        val accountNumber:String = ""
    )

    data class ReportData(
        val title:String = "خرج‌یار",
        val rangeText:String = "",
        val filtersText:String = "",
        val incomeText:String = "",
        val expenseText:String = "",
        val netText:String = "",
        val rows:List<Row>
    )

    /** سازگاری با فراخوانی و آزمون‌های قدیمی. */
    fun export(rows:List<Row>,output:OutputStream)=export(ReportData(rows=rows),output)

    fun export(data:ReportData,output:OutputStream){
        val tableHeaderRow=6
        val firstDataRow=tableHeaderRow+1
        val lastRow=(tableHeaderRow+data.rows.size).coerceAtLeast(tableHeaderRow)
        val headers=listOf("ردیف","تاریخ و ساعت","عنوان حساب","شماره حساب","نوع","دسته","شرح","مبلغ")
        ZipOutputStream(output).use{zip->
            fun entry(name:String,text:String){zip.putNextEntry(ZipEntry(name));zip.write(text.toByteArray(Charsets.UTF_8));zip.closeEntry()}
            entry("[Content_Types].xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>""")
            entry("_rels/.rels","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""")
            entry("xl/workbook.xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><bookViews><workbookView activeTab="0" firstSheet="0"/></bookViews><sheets><sheet name="گزارش خرج‌یار" sheetId="1" state="visible" r:id="rId1"/></sheets></workbook>""")
            entry("xl/_rels/workbook.xml.rels","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>""")
            entry("xl/styles.xml",stylesXml())

            fun cell(ref:String,value:String,style:Int=0)="<c r=\"$ref\" s=\"$style\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xml(value)}</t></is></c>"
            val titleRow="<row r=\"1\" ht=\"34\" customHeight=\"1\">${cell("A1",data.title,2)}</row>"
            val rangeRow="<row r=\"2\" ht=\"24\" customHeight=\"1\">${cell("A2",data.rangeText,3)}</row>"
            val filterRow="<row r=\"3\">${cell("A3",if(data.filtersText.isBlank()) "فیلترها: همه" else "فیلترها: ${data.filtersText}",0)}</row>"
            val summaryRow="<row r=\"4\" ht=\"25\" customHeight=\"1\">"+
                cell("A4","جمع واریز",4)+cell("B4",data.incomeText,5)+cell("C4","جمع برداشت",4)+cell("D4",data.expenseText,5)+cell("E4","خالص",4)+cell("F4",data.netText,5)+"</row>"
            val headerCells=headers.mapIndexed{index,value->cell("${('A'.code+index).toChar()}$tableHeaderRow",value,1)}.joinToString("")
            val headerRow="<row r=\"$tableHeaderRow\" ht=\"26\" customHeight=\"1\">$headerCells</row>"
            val dataRows=data.rows.mapIndexed{index,row->
                val ri=firstDataRow+index
                val values=listOf(row.date,row.account,row.accountNumber,row.type,row.category,row.description,row.amount)
                val automaticRowNumber="<c r=\"A$ri\"><v>${index+1}</v></c>"
                "<row r=\"$ri\">"+automaticRowNumber+values.mapIndexed{ci,value->cell("${('B'.code+ci).toChar()}$ri",value)}.joinToString("")+"</row>"
            }.joinToString("")
            val merges="<mergeCells count=\"3\"><mergeCell ref=\"A1:H1\"/><mergeCell ref=\"A2:H2\"/><mergeCell ref=\"A3:H3\"/></mergeCells>"
            entry("xl/worksheets/sheet1.xml","""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><dimension ref="A1:H$lastRow"/><sheetViews><sheetView rightToLeft="1" tabSelected="1" topLeftCell="A1" workbookViewId="0"><pane ySplit="$tableHeaderRow" topLeftCell="A$firstDataRow" activePane="bottomLeft" state="frozen"/><selection pane="bottomLeft" activeCell="A$firstDataRow" sqref="A$firstDataRow"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight="20"/><cols><col min="1" max="1" width="8" customWidth="1"/><col min="2" max="2" width="22" customWidth="1"/><col min="3" max="3" width="20" customWidth="1"/><col min="4" max="4" width="25" customWidth="1"/><col min="5" max="6" width="16" customWidth="1"/><col min="7" max="7" width="40" customWidth="1"/><col min="8" max="8" width="22" customWidth="1"/></cols><sheetData>$titleRow$rangeRow$filterRow$summaryRow$headerRow$dataRows</sheetData>$merges<autoFilter ref="A$tableHeaderRow:H$lastRow"/></worksheet>""")
        }
    }

    private fun stylesXml()="""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="3"><font><sz val="11"/><name val="Arial"/></font><font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Arial"/></font><font><b/><sz val="18"/><color rgb="FFFFFFFF"/><name val="Arial"/></font></fonts><fills count="4"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF2563EB"/><bgColor indexed="64"/></patternFill></fill><fill><patternFill patternType="solid"><fgColor rgb="FFE8F0FE"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="6"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="right" vertical="center" readingOrder="2" wrapText="1"/></xf><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center" readingOrder="2"/></xf><xf numFmtId="0" fontId="2" fillId="2" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center" readingOrder="2"/></xf><xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center" readingOrder="2"/></xf><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center" readingOrder="2"/></xf><xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyAlignment="1"><alignment horizontal="center" vertical="center" readingOrder="2"/></xf></cellXfs></styleSheet>"""
    private fun xml(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;")
}
