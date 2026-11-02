package ir.kharjyar.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModernRegistrationChoiceGuardTest {
 @Test fun requestedRegistrationChoosersUseSharedModernDialog(){
  val screens=listOf("LoansScreen.kt","DebtsScreen.kt","ChecksScreen.kt","RemindersScreen.kt","VehiclesScreen.kt","CivicCenterScreen.kt")
  screens.forEach{name->assertTrue("$name must use ModernChoiceDialog",File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText().contains("ModernChoiceDialog("))}
  val component=File("src/main/java/ir/kharjyar/app/ui/components/ModernChoiceDialog.kt").readText()
  assertTrue(component.contains("Brush.linearGradient")&&component.contains("RoundedCornerShape(28.dp)"))
 }
 @Test fun requestedEntryPagesUseModernSummaryHeroes(){
  val required=mapOf("LoansScreen.kt" to 2,"DebtsScreen.kt" to 2,"ChecksScreen.kt" to 2,"RemindersScreen.kt" to 2,"VehiclesScreen.kt" to 2,"CivicCenterScreen.kt" to 2)
  required.forEach{(name,count)->val source=File("src/main/java/ir/kharjyar/app/ui/screens/$name").readText();assertTrue("$name hero coverage",source.split("ModernSummaryHero(").size-1>=count)}
 }
}
