package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SayadCheckDetailGuardTest {
 @Test fun `clicking check opens immersive zoomable sayad visualization`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/ChecksScreen.kt").readText()
  val manifest=File("src/main/AndroidManifest.xml").readText()
  assertTrue(manifest.contains("android:configChanges=\"orientation|screenSize\""))
  listOf(
   ".clickable(onClick=onOpen)",
   "SayadCheckDetail(vm,check,settings.moneyUnit,{selectedCheck=it}){selectedCheck=null}",
   "SCREEN_ORIENTATION_SENSOR_LANDSCAPE",
   "SCREEN_ORIENTATION_UNSPECIFIED",
   "if(immersive){immersive=false}else onBack()",
   "WindowInsetsCompat.Type.systemBars()",
   "detectTapGestures{immersive=!immersive}",
   "detectTransformGestures",
   "coerceIn(1f,4f)",
   "formatSayadId(check.sayadId)",
   "check.amountInWords",
   "check.chequeAccountNumber",
   "check.chequeIban",
   "check.serialNumber"
  ).forEach{assertTrue(it,source.contains(it))}
 }
}
