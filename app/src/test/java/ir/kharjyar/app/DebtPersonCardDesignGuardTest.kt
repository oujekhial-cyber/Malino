package ir.kharjyar.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DebtPersonCardDesignGuardTest {
 @Test fun `person list cards have a simple readable hierarchy`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  listOf("شما از ${'$'}{person.name} طلبکار هستید","شما به ${'$'}{person.name} بدهکار هستید","مانده‌ای که باید بگیرید","مانده‌ای که باید بپردازید","طلب ثبت‌شده از این شخص","بدهی ثبت‌شده به این شخص","DebtClearLine(","مشاهده جزئیات").forEach{assertTrue(it,source.contains(it))}
  val listBlock=source.substringAfter("shownPeople.forEach").substringBefore("ThemedFloatingActionButton")
  assertFalse("کارت فهرست نباید دوباره به چند کارت آماری تو در تو تقسیم شود",listBlock.contains("DebtAmountTile("))
  assertFalse("گرادیان‌های چندلایه خوانایی فهرست را کم می‌کنند",listBlock.contains("Brush.linearGradient"))
 }
}
