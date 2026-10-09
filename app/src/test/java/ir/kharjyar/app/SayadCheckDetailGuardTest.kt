package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SayadCheckDetailGuardTest {
 @Test fun `clicking check opens landscape sayad cheque visualization`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  val manifest=File("src/main/AndroidManifest.xml").readText()
  assertTrue(manifest.contains("android:configChanges=\"orientation|screenSize\""))
  listOf(
   ".clickable(onClick=onOpen)",
   "SayadCheckDetail(vm,check,settings.moneyUnit,{selectedCheck=it}){selectedCheck=null}",
   "SCREEN_ORIENTATION_SENSOR_LANDSCAPE",
   "SCREEN_ORIENTATION_UNSPECIFIED",
   "BackHandler{onBack()}",
   "چک صیادی — بانک مرکزی جمهوری اسلامی ایران",
   "CheckPaperLine(\"در وجه\"",
   "CheckPaperLine(\"طرف حساب اصلی\"",
   "CheckPaperLine(\"مبلغ\"",
   "CheckPaperLine(\"تاریخ سررسید\"",
   "CheckPaperLine(\"شماره حساب\"",
   "CheckPaperLine(\"شبا\"",
   "Icons.Filled.QrCode2",
   "محل امضا"
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
