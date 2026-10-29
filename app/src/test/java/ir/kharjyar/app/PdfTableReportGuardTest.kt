package ir.kharjyar.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class PdfTableReportGuardTest {
 @Test fun `pdf report is a readable paginated rtl table`(){val s=File("src/main/java/ir/kharjyar/app/pdf/PdfExporter.kt").readText();listOf("drawTableHeader","drawTableRow","تاریخ و ساعت","حساب","نوع","دسته","شرح","مبلغ","ادامه جدول تراکنش‌ها","گزارش مالی شخصی").forEach{assertTrue(s.contains(it))};assertTrue(s.contains("TextDirectionHeuristics.RTL"));assertTrue(s.contains("if(y+height>PAGE_H-48f)nextPage()"));assertFalse(s.contains("drawLineChart"))}
}
