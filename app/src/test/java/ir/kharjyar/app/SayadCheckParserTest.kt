package ir.kharjyar.app
import ir.kharjyar.app.core.check.SayadCheckParser
import org.junit.Assert.*
import org.junit.Test
class SayadCheckParserTest {
 @Test fun `extracts labeled sayad check fields`(){val p=SayadCheckParser.parse("بانک ملی\nشناسه صیادی: ۱۲۳۴۵۶۷۸۹۰۱۲۳۴۵۶\nسریال چک: ۲۳۴۵۶۷\nمبلغ: ۴۲,۰۰۰,۰۰۰ ریال\nتاریخ ۱۴۰۵/۰۷/۱۲\nکد ملی: ۱۲۳۴۵۶۷۸۹۰\nشماره حساب: ۱۲۳-۴۵۶۷\nIR820540102680020817909002");assertEquals("1234567890123456",p.sayadId);assertEquals("234567",p.serialNumber);assertEquals(42_000_000,p.amountRial);assertEquals("ملی",p.bankName);assertEquals("1234567890",p.nationalId);assertTrue(p.iban.startsWith("IR"));assertEquals(1405,p.dueDate?.year)}
}
