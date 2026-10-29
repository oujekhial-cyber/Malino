package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class BankFeeDecimalDirectionGuardTest {
 @Test fun `bank fee accepts fractional percent and renders decimal point on physical left`() {
  val ui=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  val prefs=File("src/main/java/ir/kharjyar/app/data/prefs/SettingsRepository.kt").readText()
  val import=File("src/main/java/ir/kharjyar/app/ui/screens/SmsHistoryImportScreen.kt").readText()
  listOf("LocalLayoutDirection provides LayoutDirection.Ltr","TextAlign.Left","if(it.startsWith(\"0.\"))it.removePrefix(\"0\")","toFloatOrNull()","setBankFeePercent(value)").forEach{assertTrue(it,ui.contains(it))}
  assertTrue(prefs.contains("val bankFeePercent: Float"))
  assertTrue(prefs.contains("floatPreferencesKey(\"bank_fee_percent_decimal\")"))
  assertTrue(prefs.contains("BANK_FEE_PERCENT_LEGACY"))
  assertTrue(import.contains("base.toDouble()*feePercent/100.0"))
 }
}
