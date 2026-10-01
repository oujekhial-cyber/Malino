package ir.kharjyar.app
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
class VehicleTypeOwnerAndMotorcyclePlateGuardTest {
 @Test fun `vehicle entry supports types custom owner and visible aligned plate fields`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  listOf("خودرو سواری","موتورسیکلت","وانت","کامیون","تریلر","اتوبوس","ماشین‌آلات","شخص دیگر","نام مالک (همسر، فرزند یا شخص دیگر)","focusedTextColor=Color.Black","height(58.dp)","Text(\"ایران\",Modifier.align(Alignment.TopCenter)").forEach{assertTrue(it,screen.contains(it))}
 }
 @Test fun `motorcycle plate has three over five digits and vehicle fields persist`() {
  val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  val entity=File("src/main/java/ir/kharjyar/app/data/db/CivicEntities.kt").readText()
  val db=File("src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt").readText()
  listOf("MotorcyclePlateInput","سه رقم در ردیف بالا و پنج رقم در ردیف پایین","motorcycleDigits.takeIf{it.length==8}","it.take(3)","it.drop(3)").forEach{assertTrue(it,screen.contains(it))}
  assertTrue(entity.contains("vehicleType:String"));assertTrue(entity.contains("ownerName:String"));assertTrue(db.contains("version = 17")&&db.contains("MIGRATION_11_12")&&db.contains("MIGRATION_12_13"))
 }
}
