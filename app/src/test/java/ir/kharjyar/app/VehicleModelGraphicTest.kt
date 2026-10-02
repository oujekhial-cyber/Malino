package ir.kharjyar.app
import ir.kharjyar.app.ui.components.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class VehicleModelGraphicTest{
 @Test fun knownModelsAndTypesResolveToDifferentArtwork(){
  assertEquals(VehicleGraphicKind.PEUGEOT_PARS,vehicleGraphicKind("پژو پارس سال","خودرو سواری"))
  assertEquals(VehicleGraphicKind.PEUGEOT_206,vehicleGraphicKind("پژو ۲۰۶","خودرو سواری"))
  assertEquals(VehicleGraphicKind.SHAHIN,vehicleGraphicKind("شاهین G","خودرو سواری"))
  assertEquals(VehicleGraphicKind.MOTORCYCLE,vehicleGraphicKind("هوندا ۱۲۵","موتورسیکلت"))
  assertEquals(VehicleGraphicKind.PICKUP,vehicleGraphicKind("نیسان آبی","وانت"))
  assertEquals(VehicleGraphicKind.TRUCK,vehicleGraphicKind("ولوو","کامیون"))
 }
 @Test fun vehicleScreensUseManufacturerBadgesWithGraphicFallback(){
  val component=File("src/main/java/ir/kharjyar/app/ui/components/VehicleGraphic.kt").readText();val screen=File("src/main/java/ir/kharjyar/app/ui/screens/VehiclesScreen.kt").readText()
  assertTrue(component.contains("vehicleManufacturerLogo")&&component.contains("brand_peugeot")&&component.contains("brand_saipa")&&component.contains("brand_ikco"))
  assertTrue(component.contains("brand_mercedes")&&component.contains("brand_bmw")&&component.contains("brand_nissan")&&component.contains("brand_chery"))
  assertTrue(component.contains("brand_byd")&&component.contains("brand_lucano")&&component.contains("brand_kmc")&&component.contains("brand_bahman"))
  assertTrue(component.contains("brand_volvo")&&component.contains("brand_scania")&&component.contains("brand_faw")&&component.contains("brand_man")&&component.contains("brand_shacman"))
  assertTrue(component.contains("brand_mack")&&component.contains("brand_isuzu")&&component.contains("brand_yamaha")&&component.contains("brand_bajaj")&&component.contains("brand_ferrari"))
  assertTrue(component.contains("Canvas(")&&component.contains("quadraticBezierTo"))
  assertTrue(screen.contains("VehicleGraphic(vehicle.title,vehicle.vehicleType")&&screen.contains("VehicleGraphic(title,vehicleType"))
  assertFalse(screen.contains("Text(\"🚘\""))
 }
}
