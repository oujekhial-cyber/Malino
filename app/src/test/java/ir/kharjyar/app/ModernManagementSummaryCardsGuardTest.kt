package ir.kharjyar.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class ModernManagementSummaryCardsGuardTest{
 @Test fun sharedModernHeroHasGraphicsAndMetrics(){
  val c=File("src/main/java/ir/kharjyar/app/ui/components/ModernSummaryHero.kt").readText()
  assertTrue(c.contains("Brush.linearGradient")&&c.contains("shadowElevation=5.dp")&&c.contains("SummaryMetric"))
 }
 @Test fun requestedPagesUseModernHero(){
  val base="src/main/java/ir/kharjyar/app/ui/screens/"
  listOf("VehiclesScreen.kt","DebtsScreen.kt","LoansScreen.kt","CivicCenterScreen.kt").forEach{name->assertTrue(name,File(base+name).readText().contains("ModernSummaryHero("))}
  val v=File(base+"VehiclesScreen.kt").readText();assertTrue(v.contains("بدون جریمه جدید")&&v.contains("دارای جریمه جدید")&&v.contains("VerifiedUser")&&v.contains("WarningAmber"))
 }
}
