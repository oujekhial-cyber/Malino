package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtOriginDateGuardTest {
 @Test fun `debt entry and person cards distinguish origin date from settlement due date`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  listOf(
   "var originDate by remember",
   "تاریخ پرداخت پول و قرض‌دادن",
   "تاریخ دریافت پول و قرض‌گرفتن",
   "تاریخ سررسید تسویه",
   "val originAt = originDate.startOfDayMillis()",
   "createdAt = originAt",
   "occurredAt = originAt",
   "receivableDebts.maxOfOrNull { it.createdAt }",
   "payableDebts.maxOfOrNull { it.createdAt }",
   "PersianDate.fromMillis(it).format()"
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
