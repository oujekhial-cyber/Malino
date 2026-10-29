package ir.kharjyar.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedFinancialFlowGuardTest {
 @Test fun `one user choice maps to separate direction and nature safely`() {
  val picker=File("src/main/java/ir/kharjyar/app/ui/screens/TxForm.kt").readText()
  val manual=File("src/main/java/ir/kharjyar/app/ui/screens/ManualEntryScreen.kt").readText()
  val entity=File("src/main/java/ir/kharjyar/app/data/db/Entities.kt").readText()
  listOf("درآمد — پول وارد حساب شد","هزینه — پول از حساب خارج شد","انتقال ورودی","انتقال خروجی","واریز غیر‌درآمدی","برداشت غیرهزینه‌ای","onDirection(choice.direction);onNature(choice.nature)").forEach{assertTrue(it,picker.contains(it))}
  assertTrue(entity.contains("const val NEUTRAL = 4"))
  assertTrue(manual.contains("nature==TxNature.TRANSFER"))
  assertTrue(manual.contains("if(direction==TxDirection.DEPOSIT)viewModel.repo.addInternalTransfer"))
  assertTrue(manual.contains("nature=if(presetTransfer)TxNature.TRANSFER else nature"))
 }
}
