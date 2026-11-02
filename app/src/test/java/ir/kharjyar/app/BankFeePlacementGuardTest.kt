package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class BankFeePlacementGuardTest {
 @Test fun `bank fee card lives inside numbers and money page without separate menu`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/SettingsScreen.kt").readText()
  val numbers=source.substring(source.indexOf("if (section == \"numbers\")"),source.indexOf("// ---------- مجوزها ----------"))
  assertTrue(numbers.contains("SectionCard(\"درصد کارمزد بانکی\")"))
  assertTrue(numbers.contains("setBankFeePercent"))
  assertFalse(source.contains("nav.navigate(\"settings/bankFee\")"))
  assertFalse(source.contains("if (section == \"bankFee\")"))
 }
}
