package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class ExpressiveDetailPagesGuardTest {
 @Test fun `vehicle detail uses expressive hierarchy instead of plain gray cards`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("VehicleInfoPill","DetailSectionHeader","EmptyDetailCard","Brush.linearGradient","RoundedCornerShape(24.dp)","پرونده وسیله نقلیه").forEach{assertTrue(it,source.contains(it))}
 }
 @Test fun `loan details use elevated gradient and accented history cards`() {
  val source=File("src/main/java/ir/kharjyar/app/ui/screens/LoansScreen.kt").readText()
  listOf("Brush.linearGradient","CardDefaults.cardElevation(5.dp)","rowAccent.copy(.11f)","RoundedCornerShape(18.dp)").forEach{assertTrue(it,source.contains(it))}
 }
}
