package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FullScreenSayadChequeVisualGuardTest {
 @Test fun `saved cheque fields overlay exact raw sayad layout without receipt stub`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("R.drawable.sayad_cheque_template"))
  assertTrue(source.contains("\"تصویر خام چک صیادی\""))
  assertTrue(source.contains("aspectRatio(2.066f)"))
  assertTrue(source.contains("Modifier.offset(x(.68f),y(.105f))"))
  assertTrue(source.contains("Modifier.offset(x(.18f),y(.34f))"))
  assertTrue(source.contains("Modifier.offset(x(.19f),y(.48f))"))
  assertTrue(source.contains("Modifier.offset(x(.055f),y(.665f))"))
  assertTrue(source.contains("formatSayadId(check.sayadId)"))
  assertTrue(source.contains("check.amountInWords"))
  assertTrue(source.contains("check.chequeAccountNumber"))
  assertTrue(source.contains("check.chequeIban"))
  assertTrue(source.contains("check.nationalId"))
  assertTrue(source.contains("check.serialNumber"))
 }
}
