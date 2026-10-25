package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FullScreenSayadChequeVisualGuardTest {
 @Test fun `full screen check resembles sayad document and places saved fields on it`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  assertTrue(source.contains("SCREEN_ORIENTATION_SENSOR_LANDSCAPE"))
  assertTrue(source.contains("R.drawable.sayad_cheque_template"))
  assertTrue(source.contains("contentDescription=\"تصویر خام چک صیادی\""))
  assertTrue(source.contains("ContentScale.FillBounds"))
  assertTrue(source.contains("شناسه ۱۶ رقمی صیاد"))
  assertTrue(source.contains("formatSayadId(check.sayadId)"))
  assertTrue(source.contains("مبلغ به عدد"))
  assertTrue(source.contains("مبلغ به حروف"))
  assertTrue(source.contains("check.amountInWords"))
  assertTrue(source.contains("check.chequeAccountNumber"))
  assertTrue(source.contains("check.chequeIban"))
  assertTrue(source.contains("check.nationalId"))
  assertTrue(source.contains("check.serialNumber"))
  assertTrue(source.contains("محل امضا"))
 }
}
