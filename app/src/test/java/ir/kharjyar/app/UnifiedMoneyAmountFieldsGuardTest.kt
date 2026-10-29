package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class UnifiedMoneyAmountFieldsGuardTest {
 @Test fun `standard amount field groups thousands and displays selected unit`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/components/AmountInput.kt").readText()
  assertTrue(source.contains("visualTransformation = ThousandsSeparatorTransformation()"))
  assertTrue(source.contains("unit: MoneyUnit? = null"))
  assertTrue(source.contains("if (unit == MoneyUnit.TOMAN) \"تومان\" else \"ریال\""))
 }
 @Test fun `money forms pass selected unit and convert input to rial storage`() {
  val screens=listOf("DebtsScreen.kt","LoansScreen.kt","ChecksScreen.kt","CivicCenterScreen.kt","AssetsScreen.kt","AccountEditScreen.kt","ManualEntryScreen.kt","TransactionEditScreen.kt")
  screens.forEach{name->
   val source=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText()
   assertTrue("unit missing in $name",source.contains("unit=settings.moneyUnit")||source.contains("unit = settings.moneyUnit")||name=="AssetsScreen.kt"&&source.contains("unit=unit")||name=="CivicCenterScreen.kt")
  }
  val debt=File("src/main/java/ir/kharjyar/app/ui/screens/DebtsScreen.kt").readText()
  assertTrue(debt.contains("Money.inputToRial(amount, settings.moneyUnit)"))
  assertFalse(debt.contains("\"مبلغ به ریال\""))
 }
}
